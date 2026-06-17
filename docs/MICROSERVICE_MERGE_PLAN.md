# 微服务融合方案

## 一、现状分析

### 1.1 当前服务列表

| 服务 | Java文件数 | Controller数 | 职责 |
|------|-----------|--------------|------|
| gateway-service | 1 | 0 | API 网关路由 |
| auth-service | 19 | 2 | 认证授权、Token 管理 |
| system-service | 71 | 12 | 用户、角色、菜单、字典、日志 |
| file-service | 9 | 2 | 文件上传下载（MinIO） |
| blog-service | 59 | 14 | 文章、分类、标签、评论、点赞、收藏 |
| search-service | 25 | 2 | 全文搜索、向量检索 |
| ai-service | 37 | 8 | AI 对话、生成、Prompt 管理 |
| knowledge-service | 31 | 4 | 知识库、文档、RAG 检索 |
| project-service | 60 | 10 | 项目、任务、Bug、执行记录 |
| agent-service | 33 | 3 | Agent 执行、代码生成、工作区管理 |
| mcp-service | 32 | 4 | MCP Server/Tool 管理 |

**总计**：11 个服务，377 个 Java 文件，61 个 Controller

---

## 二、融合方案

### 2.1 融合原则

1. **最小化改动**：只融合代码量少、依赖关系简单的服务
2. **保持独立性**：核心业务服务保持独立
3. **减少服务数**：合并资源浪费的服务

### 2.2 融合方案

```
原 11 个服务 → 融合为 9 个服务
```

#### 融合详情

| 融合后服务 | 融合内容 | 说明 |
|-----------|---------|------|
| **gateway-service** | 不变 | API 网关路由 |
| **auth-service** | 不变 | 认证授权 |
| **system-service** | ← file-service | 系统管理 + 文件管理 |
| **blog-service** | 不变 | 博客管理 |
| **search-service** | 不变 | 搜索服务 |
| **ai-service** | ← knowledge-service | AI 服务 + 知识库 |
| **project-service** | 不变 | 项目管理 |
| **agent-service** | 不变 | Agent 执行 |
| **mcp-service** | 不变 | MCP 管理 |

---

## 三、融合详情

### 3.1 system-service ← file-service

**理由**：
- file-service 仅 9 个 Java 文件，代码量最少
- 文件管理与系统管理关联紧密（头像上传、附件管理）
- 减少一个独立服务，节省资源

**融合内容**：
```
com.platform.file → com.platform.system.file
```

**迁移文件**：
- `FileController.java`
- `FileService.java`
- `FileMapper.java`
- `SysFile.java`（实体）
- `FileVO.java`（VO）
- `MinioConfig.java`
- `MinioService.java`

**API 路径**：
- 保持不变：`/api/file/**`
- 网关路由指向 system-service

**配置迁移**：
```yaml
# MinIO 配置迁移到 system-service
minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket: platform
```

---

### 3.2 ai-service ← knowledge-service

**理由**：
- knowledge-service 依赖 ai-service 的 Embedding 能力
- RAG 检索与 AI 对话紧密关联
- 合并后减少跨服务调用，提升性能

**融合内容**：
```
com.platform.knowledge → com.platform.ai.knowledge
```

**迁移文件**：
- `KnowledgeController.java`
- `DocumentController.java`
- `DocumentService.java`
- `DocumentProcessingService.java`
- `EmbeddingService.java`
- `KnowledgeService.java`
- 相关实体、Mapper、DTO/VO

**API 路径**：
- 保持不变：`/api/knowledge/**`
- 网关路由指向 ai-service

**配置迁移**：
```yaml
# pgvector 配置迁移到 ai-service
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/platform
  ai:
    vectorstore:
      pgvector:
        table-name: kb_embedding
        index-type: hnsw
```

---

## 四、融合后的服务架构

```
┌─────────────────────────────────────────────────────────────────┐
│                          前端                                    │
│   platform-web (管理后台)  |  blog-web (公开博客)                │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                    ┌───────▼───────┐
                    │ gateway-service│  (API 网关, :1080)
                    └───────┬───────┘
                            │
    ┌───────────┬───────────┼───────────┬───────────┐
    │           │           │           │           │
┌───▼───┐  ┌───▼───┐  ┌───▼───┐  ┌───▼───┐  ┌───▼───┐
│ auth  │  │system │  │ blog  │  │  ai   │  │project│
│:1081  │  │:1082  │  │:1083  │  │:1086  │  │:1088  │
└───────┘  └───┬───┘  └───────┘  └───┬───┘  └───────┘
               │                     │
           file-service      knowledge-service
           （已融合）            （已融合）

    ┌───────────┐  ┌───────────┐  ┌───────────┐
    │  search   │  │   agent   │  │    mcp    │
    │   :1085   │  │   :1090   │  │   :1089   │
    └───────────┘  └───────────┘  └───────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      共享基础设施                                │
│  PostgreSQL │ Redis │ Nacos │ MinIO │ ES │ Kafka │ pgvector    │
└─────────────────────────────────────────────────────────────────┘
```

---

## 五、网关路由更新

### 5.1 融合后路由配置

```yaml
spring:
  cloud:
    gateway:
      routes:
        # 认证服务
        - id: auth-service
          uri: lb://auth-service
          predicates: Path=/api/auth/**
        
        # 系统服务（含文件服务）
        - id: system-service
          uri: lb://system-service
          predicates: Path=/api/system/**,/api/file/**
        
        # 博客服务
        - id: blog-service
          uri: lb://blog-service
          predicates: Path=/api/blog/**
        
        # 搜索服务
        - id: search-service
          uri: lb://search-service
          predicates: Path=/api/search/**
        
        # AI 服务（含知识库）
        - id: ai-service
          uri: lb://ai-service
          predicates: Path=/api/ai/**,/api/knowledge/**
        
        # 项目服务
        - id: project-service
          uri: lb://project-service
          predicates: Path=/api/project/**
        
        # Agent 服务
        - id: agent-service
          uri: lb://agent-service
          predicates: Path=/api/agent/**
        
        # MCP 服务
        - id: mcp-service
          uri: lb://mcp-service
          predicates: Path=/api/mcp/**
```

---

## 六、前端调整

### 6.1 不需要调整的 API

| 路径 | 说明 |
|------|------|
| `/api/auth/**` | 认证授权 |
| `/api/system/**` | 系统管理 |
| `/api/file/**` | 文件管理（路径不变） |
| `/api/blog/**` | 博客管理 |
| `/api/search/**` | 搜索服务 |
| `/api/ai/**` | AI 服务 |
| `/api/knowledge/**` | 知识库（路径不变） |
| `/api/project/**` | 项目管理 |
| `/api/agent/**` | Agent 服务 |
| `/api/mcp/**` | MCP 服务 |

**结论**：前端代码无需修改，API 路径保持不变。

---

## 七、实施计划

### 7.1 阶段一：准备（0.5 天）

- [ ] 备份当前代码
- [ ] 创建 Git 分支 `feature/service-merge`

### 7.2 阶段二：融合 file-service → system-service（1 天）

- [ ] 迁移 Java 文件
- [ ] 迁移配置文件
- [ ] 更新 pom.xml 依赖
- [ ] 编译测试

### 7.3 阶段三：融合 knowledge-service → ai-service（1 天）

- [ ] 迁移 Java 文件
- [ ] 迁移配置文件
- [ ] 更新 pom.xml 依赖
- [ ] 编译测试

### 7.4 阶段四：更新网关配置（0.5 天）

- [ ] 更新路由配置
- [ ] 更新 Nacos 服务名
- [ ] 测试路由

### 7.5 阶段五：测试验证（1 天）

- [ ] 全量编译检查
- [ ] 功能回归测试
- [ ] 性能测试

**总工期**：约 4 天

---

## 八、风险评估

| 风险 | 影响 | 应对措施 |
|------|------|----------|
| 代码冲突 | 低 | 使用 Git 分支管理 |
| 配置遗漏 | 低 | 详细检查配置文件 |
| API 路径变更 | 无 | 保持路径不变 |
| 数据库变更 | 无 | 不涉及数据库变更 |

---

## 九、预期收益

| 指标 | 融合前 | 融合后 | 改善 |
|------|--------|--------|------|
| 服务数量 | 11 | 9 | -18% |
| 启动时间 | ~5min | ~4.5min | -10% |
| 内存占用 | ~11GB | ~10GB | -9% |
| 跨服务调用 | 多 | 少 | 简化 |

---

## 十、审批确认

请审阅以上方案，确认后开始执行。

**需要确认的问题**：

1. 是否同意融合方案？
2. 是否需要保留原服务目录作为备用？
3. 是否需要更新 Docker Compose 配置？
