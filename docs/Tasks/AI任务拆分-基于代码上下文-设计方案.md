# AI 任务拆分 - 基于代码上下文 - 设计方案

## 1. 需求概述

| 需求 | 说明 |
|------|------|
| 项目添加 gitUrl 字段 | 每个项目关联一个 Git 仓库地址 |
| 拉取代码到工作区 | AI 拆分前先 clone 项目代码 |
| 读取代码上下文 | 读取目录结构、技术栈、关键代码 |
| 基于代码拆分任务 | 将代码上下文传给 AI，拆分更精准 |

## 2. 整体流程

```
用户点击"AI拆分"
    │
    ▼
ProjectServiceImpl.aiDecomposeTasks()
    │
    ▼
AiTaskServiceImpl.decompose()
    │
    ├─ 1. 调用 agent-service 获取项目上下文
    │     └─ AgentServiceClient.getProjectContext(projectId, gitUrl)
    │           └─ HTTP GET http://agent-service/projects/{id}/context?gitUrl=xxx
    │
    ├─ 2. agent-service 内部处理
    │     ├─ 检查工作区是否已 clone
    │     ├─ 未 clone → git clone 到 workspace/project-{id}/
    │     └─ 调用 ProjectContextService.getFullContext()
    │           ├─ 读取目录结构（3层深度）
    │           ├─ 读取 pom.xml / package.json（技术栈）
    │           └─ 读取关键代码示例（Controller、Service）
    │
    ├─ 3. 拼接 prompt（需求 + 代码上下文）
    │
    └─ 4. 调用 AI 拆分
```

## 3. 数据库变更

### 3.1 pm_project 表添加 git_url 列

```sql
ALTER TABLE pm_project ADD COLUMN git_url VARCHAR(500);
COMMENT ON COLUMN pm_project.git_url IS 'Git 仓库地址';
```

## 4. 后端改动

### 4.1 Project 实体

**文件**: `services/project-service/.../entity/Project.java`

```java
/**
 * Git 仓库地址
 */
private String gitUrl;
```

### 4.2 DTO/VO 同步

| 文件 | 改动 |
|------|------|
| `ProjectCreateDTO.java` | 添加 `gitUrl` 字段 |
| `ProjectUpdateDTO.java` | 添加 `gitUrl` 字段 |
| `ProjectVO.java` | 添加 `gitUrl` 字段 |

> MapStruct 自动映射同名字段，无需额外配置。

### 4.3 agent-service 添加项目上下文 API

**文件**: `services/agent-service/.../controller/AgentController.java`

新增端点：

```java
@Operation(summary = "获取项目代码上下文")
@GetMapping("/projects/{projectId}/context")
public Result<String> getProjectContext(
        @PathVariable Long projectId,
        @RequestParam String gitUrl) {
    String context = projectContextService.getFullContextWithGit(projectId, gitUrl);
    return Result.success(context);
}
```

**文件**: `services/agent-service/.../service/ProjectContextService.java`

新增方法：

```java
/**
 * 获取项目完整上下文（自动 clone）
 * @param projectId 项目ID
 * @param gitUrl Git 仓库地址
 * @return 完整上下文
 */
String getFullContextWithGit(Long projectId, String gitUrl);
```

**文件**: `services/agent-service/.../service/impl/ProjectContextServiceImpl.java`

实现逻辑：

```java
@Override
public String getFullContextWithGit(Long projectId, String gitUrl) {
    String workspacePath = getWorkspacePath(projectId);
    Path root = Paths.get(workspacePath);

    // 1. 如果工作区不存在，clone 代码
    if (!Files.exists(root)) {
        log.info("工作区不存在，开始 clone: {}", gitUrl);
        mcpToolService.mkdir(workspacePath);
        mcpToolService.gitClone(gitUrl, workspacePath);
    }

    // 2. 读取完整上下文
    return getFullContext(projectId);
}
```

### 4.4 project-service 调用 agent-service

**新增文件**: `services/project-service/.../client/AgentServiceClient.java`

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class AgentServiceClient {

    private final RestTemplate restTemplate;

    /**
     * 获取项目代码上下文
     */
    public String getProjectContext(Long projectId, String gitUrl) {
        try {
            String url = "http://agent-service/projects/" + projectId
                    + "/context?gitUrl=" + java.net.URLEncoder.encode(gitUrl, "UTF-8");
            ResponseEntity<Result<String>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<Result<String>>() {}
            );
            Result<String> result = response.getBody();
            if (result != null && result.getCode() == 200) {
                return result.getData();
            }
            return null;
        } catch (Exception e) {
            log.warn("获取项目上下文失败, projectId: {}", projectId, e);
            return null;
        }
    }
}
```

**新增文件**: `services/project-service/.../config/RestTemplateConfig.java`

```java
@Configuration
public class RestTemplateConfig {
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### 4.5 AiTaskServiceImpl 改造

**文件**: `services/project-service/.../service/impl/AiTaskServiceImpl.java`

核心改动 - `buildDecomposePrompt()` 方法：

```java
private String buildDecomposePrompt(AiTaskDecomposeDTO dto) {
    String content = dto.getContent();
    String techStack = dto.getTechStack();
    String projectContext = dto.getProjectContext();  // 新增

    // 技术栈上下文
    String techContext = "";
    if (techStack != null && !techStack.isEmpty()) {
        techContext = "\n## 项目技术栈\n" + techStack + "\n";
    }

    // 代码上下文（新增）
    String codeContext = "";
    if (projectContext != null && !projectContext.isEmpty()) {
        codeContext = "\n## 项目代码结构与上下文\n" + projectContext + "\n";
    }

    return String.format(
        "你是一个资深的软件项目经理和技术架构师。\n" +
        "请根据以下需求和项目现有代码，拆解出具体可执行的开发任务。\n\n" +
        "## 需求内容\n%s\n\n" +
        "%s%s%s" +   // techContext + codeContext + 其他参数
        "## 拆解要求\n" +
        "1. 任务必须基于现有代码结构，在正确的模块和包下创建/修改文件\n" +
        "2. 任务必须是具体的、可执行的开发任务\n" +
        "3. 每个任务应包含要修改/创建的具体文件路径\n" +
        "4. 任务之间要有合理的执行顺序\n" +
        // ... 其他要求不变
        content, techContext, codeContext, maxTasksHint, granularityHint
    );
}
```

**文件**: `services/project-service/.../service/impl/ProjectServiceImpl.java`

在 `aiDecomposeTasks()` 中获取上下文：

```java
@Override
public List<Long> aiDecomposeTasks(Long projectId, String content, ...) {
    Project project = projectMapper.selectById(projectId);
    // ...

    // 获取代码上下文（新增）
    String projectContext = null;
    if (project.getGitUrl() != null && !project.getGitUrl().isEmpty()) {
        projectContext = agentServiceClient.getProjectContext(projectId, project.getGitUrl());
    }

    AiTaskDecomposeDTO dto = new AiTaskDecomposeDTO();
    dto.setProjectId(projectId);
    dto.setContent(content);
    dto.setTechStack(techStack);
    dto.setMaxTasks(maxTasks);
    dto.setGranularity(granularity);
    dto.setProjectContext(projectContext);  // 新增

    return aiTaskService.decomposeAndCreate(dto);
}
```

## 5. 前端改动

### 5.1 API 类型

**文件**: `frontend/platform-web/src/api/project.ts`

```typescript
// ProjectCreateParams 添加
gitUrl?: string

// ProjectUpdateParams 添加
gitUrl?: string

// ProjectVO 添加
gitUrl: string
```

### 5.2 项目表单

**文件**: `frontend/platform-web/src/modules/project/views/ProjectListView.vue`

在描述字段后添加 Git 地址输入框：

```vue
<NFormItem label="Git地址">
  <NInput v-model:value="formData.gitUrl" placeholder="https://github.com/xxx/xxx.git" clearable />
</NFormItem>
```

## 6. 涉及文件清单

| 服务 | 文件 | 改动类型 |
|------|------|----------|
| SQL | `sql/init.sql` | 添加 git_url 列 |
| project-service | `entity/Project.java` | 添加字段 |
| project-service | `dto/ProjectCreateDTO.java` | 添加字段 |
| project-service | `dto/ProjectUpdateDTO.java` | 添加字段 |
| project-service | `vo/ProjectVO.java` | 添加字段 |
| project-service | `client/AgentServiceClient.java` | **新增** |
| project-service | `config/RestTemplateConfig.java` | **新增** |
| project-service | `service/impl/AiTaskServiceImpl.java` | 改造 prompt |
| project-service | `service/impl/ProjectServiceImpl.java` | 获取上下文 |
| agent-service | `controller/AgentController.java` | 添加 API |
| agent-service | `service/ProjectContextService.java` | 添加方法 |
| agent-service | `service/impl/ProjectContextServiceImpl.java` | 实现 clone+读取 |
| 前端 | `api/project.ts` | 添加 gitUrl 类型 |
| 前端 | `views/ProjectListView.vue` | 添加表单字段 |

## 7. 验证方式

1. 创建项目时填写 gitUrl（如 `https://github.com/liushulun777/personal-ai-platform.git`）
2. 点击 AI 拆分，观察 agent-service 日志输出 clone 和目录读取信息
3. 检查拆分结果是否包含具体的文件路径和代码相关任务
4. 第二次拆分同一项目时应复用已有工作区（不重复 clone）
