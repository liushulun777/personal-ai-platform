# 微服务核心概念与实践指南

> 以 Personal AI Platform 系统为实例，深入理解微服务架构的核心概念。
> 本文档用于知识学习和 AI 代码生成参考。

---

## 一、服务拆分（Service Decomposition）

### 核心概念
将单体应用按业务领域拆分为独立的、可独立部署的服务。每个服务拥有自己的代码库、数据库和生命周期。

### 拆分原则
- **单一职责**：每个服务只负责一个业务领域
- **高内聚低耦合**：服务内部紧密关联，服务之间松散依赖
- **数据自治**：每个服务拥有自己的数据库，不直接访问其他服务的数据库

### 本系统实例

```
personal-ai-platform/
├── gateway-service      # API 网关 — 路由、负载均衡、跨域
├── auth-service         # 认证服务 — 登录、注册、Token 管理
├── system-service       # 系统服务 — 用户、角色、菜单、字典、日志
├── blog-service         # 博客服务 — 文章、分类、标签、评论、点赞
├── search-service       # 搜索服务 — Elasticsearch 全文搜索 + pgvector 语义搜索
├── ai-service           # AI 服务 — 对话、知识库、文档处理、Prompt 模板
├── project-service      # 项目服务 — 项目、任务、Bug 管理
├── mcp-service          # MCP 服务 — MCP Server/Tool 注册与调用
├── agent-service        # Agent 服务 — 任务自动执行、代码生成、Git 操作
└── common/              # 共享模块（不是独立服务）
    ├── common-core      # 基础工具：实体基类、异常、Result、分页
    ├── common-web       # Web 基础：全局异常、CORS、MyBatis 配置、限流
    ├── common-security  # 安全基础：Sa-Token 配置、权限注解、数据权限
    └── common-ai        # AI 基础：AI 模型配置、统一 AI 服务接口
```

### 思考题
1. 为什么 `blog-service` 和 `search-service` 要分开，而不是合在一起？
2. `common` 模块为什么不是独立服务？它和服务的区别是什么？
3. 如果要新增一个「评论审核」功能，应该放在哪个服务里？

---

## 二、API 网关（API Gateway）

### 核心概念
网关是系统的统一入口，负责请求路由、负载均衡、认证鉴权、限流、跨域处理等横切关注点。

### 关键职责
| 职责 | 说明 |
|------|------|
| 路由转发 | 根据 URL 路径将请求转发到对应服务 |
| 负载均衡 | `lb://service-name` 使用 Ribbon/LoadBalancer 在多个实例间分配请求 |
| 路径改写 | `StripPrefix=1` 去掉路径前缀（`/blog/articles` → `/articles`） |
| 跨域处理 | 统一配置 CORS 头 |
| 认证透传 | 将 Token 透传到下游服务 |

### 本系统实例

```yaml
# gateway-service/application.yml
spring:
  cloud:
    gateway:
      routes:
        - id: blog-service
          uri: lb://blog-service          # 负载均衡：通过 Nacos 发现服务实例
          predicates:
            - Path=/blog/**               # 匹配路径
          filters:
            - StripPrefix=1               # 去掉 /blog 前缀

        - id: ai-service
          uri: lb://ai-service
          predicates:
            - Path=/ai/**,/knowledge/**   # 一个服务可以有多个路径前缀
          filters:
            - StripPrefix=1
```

### 请求流转示例
```
前端请求: GET /blog/articles/123
    ↓
Gateway: 匹配 Path=/blog/** → StripPrefix=1
    ↓
转发到: lb://blog-service/articles/123
    ↓
BlogController: @GetMapping("/{id}") → getById(123)
```

### 思考题
1. 如果 `blog-service` 有 3 个实例，网关如何决定请求发给哪个？
2. 为什么需要 `StripPrefix`？不配置会怎样？
3. 网关层能做权限校验吗？和在服务内做有什么区别？

---

## 三、服务注册与发现（Service Registry & Discovery）

### 核心概念
服务启动时自动注册到注册中心（如 Nacos），其他服务通过服务名查找实例地址，无需硬编码 IP。

### 本系统实例

```yaml
# 每个服务的 application.yml 都有：
spring:
  cloud:
    nacos:
      discovery:
        server-addr: 172.18.0.34:8848   # Nacos 注册中心地址
```

```
Nacos 注册中心
├── auth-service      → [172.18.0.34:1081]
├── system-service    → [172.18.0.34:1082]
├── blog-service      → [172.18.0.34:1083]
├── ai-service        → [172.18.0.34:1086]
├── project-service   → [172.18.0.34:1088]
└── ...
```

### Feign 调用时的地址解析
```java
// blog-service 调用 system-service 获取用户信息
@FeignClient(name = "system-service")  // ← 通过服务名查找，不是 IP
public interface UserServiceClient {
    @GetMapping("/users/{id}")
    UserDTO getUserDTOById(@PathVariable("id") Long id);
}
```

```
blog-service 发起调用: http://system-service/users/123
    ↓
LoadBalancer 从 Nacos 获取 system-service 的实例列表
    ↓
选择一个实例: http://172.18.0.34:1082/users/123
```

### 思考题
1. 如果 system-service 有多个实例，Feign 默认用什么策略选择？
2. 服务下线后，Nacos 多久能感知？这段时间内调用会怎样？
3. `fail-fast: false` 配置的含义是什么？

---

## 四、服务间通信（Inter-Service Communication）

### 4.1 同步通信：Feign / RestTemplate

**适用场景**：需要立即获取结果的请求-响应模式。

### 本系统实例

```java
// blog-service 需要获取用户信息来填充文章作者名
@FeignClient(name = "system-service")
public interface UserServiceClient {
    @GetMapping("/users/{id}")
    Result<UserDTO> getUserDTOById(@PathVariable("id") Long id);
}

// 使用方式
UserDTO user = userServiceClient.getUserDTOById(article.getAuthorId());
articleVO.setAuthorName(user.getNickname());
```

```
blog-service                          system-service
    │                                      │
    │  GET /users/123                      │
    │ ──────────────────────────────────→  │
    │                                      │  查数据库
    │  200 OK { "nickname": "张三" }       │
    │ ←──────────────────────────────────  │
    │                                      │
    │  填充 authorName = "张三"            │
```

### 4.2 异步通信：Kafka 消息队列

**适用场景**：不需要立即结果、解耦生产者和消费者、支持重试。

### 本系统实例

```
blog-service                              search-service
    │                                          │
    │  文章发布                                 │
    │  kafkaTemplate.send("article-publish")   │
    │ ───────────────── Kafka ──────────────→  │
    │                                          │  @KafkaListener
    │                                          │  索引到 Elasticsearch
    │                                          │  分块 + 向量嵌入到 pgvector
    │                                          │
    │  继续处理其他逻辑（不等待）               │
```

### 同步 vs 异步对比

| 维度 | 同步 (Feign) | 异步 (Kafka) |
|------|-------------|-------------|
| 响应时间 | 受下游服务影响 | 立即返回 |
| 耦合度 | 强耦合（依赖下游可用性） | 松耦合 |
| 错误处理 | 直接抛异常 | 重试、死信队列 |
| 适用场景 | 查询、需要结果 | 事件通知、最终一致性 |

### 思考题
1. 为什么文章发布用 Kafka 异步通知搜索服务，而不是 Feign 同步调用？
2. 如果 Kafka 消费者处理失败怎么办？消息会丢失吗？
3. 什么场景下应该选择同步通信？

---

## 五、API 网关路由与路径设计

### 路径设计规范

```
前端请求路径 = 网关前缀 + 服务内部路径

示例：
GET  /blog/articles         → blog-service  /articles         (列表)
GET  /blog/articles/123     → blog-service  /articles/123     (详情)
POST /blog/articles         → blog-service  /articles         (创建)
GET  /system/users          → system-service /users           (用户列表)
POST /ai/conversations/chat → ai-service    /conversations/chat (AI对话)
```

### 本系统路由表

| 网关路径前缀 | 目标服务 | 说明 |
|-------------|---------|------|
| `/auth/**` | auth-service | 认证（登录/注册/登出） |
| `/system/**` | system-service | 用户/角色/菜单/字典/日志 |
| `/blog/**` | blog-service | 文章/分类/标签/评论 |
| `/search/**` | search-service | 搜索 |
| `/ai/**` | ai-service | AI 对话/生成 |
| `/knowledge/**` | ai-service | 知识库（同一服务，不同路径） |
| `/project/**` | project-service | 项目/任务/Bug |
| `/mcp/**` | mcp-service | MCP 服务/工具 |
| `/agent/**` | agent-service | Agent 执行 |

### 思考题
1. 为什么 `ai-service` 有两个路径前缀 `/ai/**` 和 `/knowledge/**`？
2. 前端的 API 路径怎么和网关路由对应？（如 `/api/blog/articles` → `/blog/articles`）
3. 如果要给搜索服务加一个 `/search/suggestions` 接口，网关需要改配置吗？

---

## 六、统一响应与异常处理

### 核心概念
所有服务使用统一的响应格式和异常处理机制，前端只需解析一种格式。

### 本系统实例

```java
// 统一响应格式
public class Result<T> {
    private int code;       // 200=成功, 其他=失败
    private String message; // 提示信息
    private T data;         // 业务数据
}

// 使用方式
@GetMapping("/{id}")
public Result<ArticleDetailVO> getById(@PathVariable Long id) {
    ArticleDetailVO detail = articleService.getById(id);
    return Result.success(detail);
}
```

### 全局异常处理链

```
Controller 抛出异常
    ↓
GlobalExceptionHandler（common-web）
├── BusinessException → 返回 Result(code=业务错误码, message=错误信息)
├── MethodArgumentNotValidException → 返回 Result(code=400, message=参数校验失败)
├── HttpRequestMethodNotSupportedException → 405
└── Exception → 500 系统错误
```

### 思考题
1. 为什么用 `Result<T>` 包装所有响应，而不是直接返回 VO？
2. `ResultCode` 枚举应该包含哪些常见错误码？
3. 前端如何根据 `code` 判断请求是否成功？

---

## 七、认证与授权（Authentication & Authorization）

### 7.1 认证：Token 机制

```
用户登录 → auth-service 生成 Token → 前端存储 Token
    ↓
后续请求 → 携带 Token → 网关/服务验证 Token → 获取用户身份
```

### 本系统认证流程

```
1. 前端 POST /auth/login { username, password }
2. auth-service 验证密码 → StpUtil.login(userId) → 生成 Token 存入 Redis
3. 前端收到 Token，存入 localStorage
4. 后续请求 Header: Authorization: <token>
5. 请求到达服务 → Sa-Token 拦截器验证 Token → 从 Redis 获取 userId
```

### 7.2 授权：RBAC 权限模型

```
用户 ──→ 用户角色关联 ──→ 角色 ──→ 角色菜单关联 ──→ 菜单（权限标识）
                                      │
                                      └──→ 按钮权限（如 system:user:add）
```

### 权限校验流程

```
请求到达 Controller
    ↓
@RequirePermission("blog:article:edit")  ← 注解声明需要的权限
    ↓
PermissionAspect 切面拦截
    ↓
StpUtil.hasPermission("blog:article:edit")
    ↓
StpInterfaceImpl.getPermissionList(userId)
    ↓
查询: sys_user_role → sys_role_menu → sys_menu.permission
    ↓
返回用户拥有的权限列表
    ↓
比对: 用户权限中是否包含 "blog:article:edit"
```

### 数据权限

```java
// 不同角色看到不同范围的数据
// 管理员：看到所有文章
// 作者：只看到自己的文章

@DataScopeUtils.applyDataScope(wrapper, Article::getAuthorId, currentUserId)
// 内部逻辑：
// if (hasAllDataScope()) → 不加过滤条件
// else → wrapper.eq(authorField, currentUserId)
```

### 思考题
1. Sa-Token 的 Token 存在哪里？为什么用 Redis？
2. 如果用户权限变更了，已有的 Token 会立即生效吗？
3. 数据权限和功能权限的区别是什么？

---

## 八、事件驱动架构（Event-Driven Architecture）

### 核心概念
服务之间通过事件（消息）通信，生产者发送事件到消息队列，消费者异步处理。

### 本系统事件流

```
┌─────────────────────────────────────────────────────────────┐
│                    Kafka 事件总线                             │
├──────────────────┬──────────────────┬───────────────────────┤
│ article-publish  │ article-update   │ article-delete        │
│ (文章发布)       │ (文章更新)        │ (文章删除)            │
├──────────────────┴──────────────────┴───────────────────────┤
│                    task-created                              │
│ (任务创建 → Agent 自动执行)                                  │
└─────────────────────────────────────────────────────────────┘

生产者:
  blog-service    → article-publish / article-update / article-delete
  project-service → task-created

消费者:
  search-service  ← article-publish / article-update / article-delete
                    (索引到 ES + pgvector)
  agent-service   ← task-created
                    (自动执行代码生成)
```

### 事件处理模式

```java
// 生产者：事务提交后发送事件（避免事务回滚但消息已发）
@Transactional
public void publish(Long id) {
    article.setStatus(1);
    articleMapper.updateById(article);
    
    // 注册事务同步回调
    TransactionSynchronizationManager.registerSynchronization(
        new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                articleEventPublisher.publishArticlePublishEvent(event);
            }
        });
}

// 消费者：幂等处理 + 手动确认
@KafkaListener(topics = "article-publish")
public void handle(@Payload String payload) {
    ArticleDocument doc = articleEventConvert.parseToDocument(payload);
    searchService.indexArticle(doc);        // 索引到 ES
    articleChunkService.processArticle(doc); // 分块 + 向量化
}
```

### 思考题
1. 为什么用 `afterCommit` 而不是在事务内直接发 Kafka？
2. 消费者处理失败怎么办？消息会丢失吗？
3. 同一个 Topic 可以有多个消费者组吗？

---

## 九、数据隔离与数据库设计

### 核心概念
每个服务拥有自己的数据表，不直接访问其他服务的数据库。

### 本系统数据归属

| 服务 | 数据表 | 说明 |
|------|--------|------|
| auth-service | sys_user, sys_role, sys_user_role | 认证相关 |
| system-service | sys_user, sys_role, sys_menu, sys_dict_* | 系统管理 |
| blog-service | biz_article, biz_category, biz_tag, biz_comment, biz_like, biz_favorite | 博客 |
| ai-service | ai_conversation, ai_message, ai_prompt, kb_* | AI 与知识库 |
| project-service | pm_project, pm_task, pm_bug, pm_task_comment | 项目管理 |
| search-service | biz_article_chunk (pgvector), articles (ES索引) | 搜索 |
| mcp-service | mcp_server, mcp_tool, mcp_resource | MCP 平台 |

### 跨服务数据获取

```
blog-service 需要用户昵称 → 不能直接查 sys_user 表
    ↓
通过 Feign 调用 system-service 的 /users/{id} 接口
    ↓
system-service 查自己的数据库返回 UserDTO
```

### 思考题
1. 为什么 `sys_user` 表被 auth-service 和 system-service 共用？
2. 如果要查「某用户发布的所有文章」，应该调哪个服务的接口？
3. 如何避免跨服务调用的 N+1 问题？（参考 `fillArticleVOBatch` 批量查询）

---

## 十、共享模块设计（Common Modules）

### 核心概念
将通用功能抽取到共享模块，避免代码重复，保证一致性。

### 本系统 common 模块

```
common-core（最底层，无外部依赖）
├── BaseEntity          # 所有实体的基类（id, createTime, updateTime, deleted）
├── Result<T>           # 统一响应封装
├── PageQuery           # 分页查询基类
├── BusinessException   # 业务异常
├── SysUserRole/SysRoleMenu  # 权限关联实体（被所有服务共享）
└── SysUserRoleMapper   # 权限查询 Mapper（含自定义 SQL）

common-security（依赖 common-core）
├── SaTokenConfig       # Sa-Token 拦截器配置
├── StpInterfaceImpl    # 权限数据源实现（所有服务共享）
├── RequirePermission   # 权限注解
├── RequireRole         # 角色注解
├── DataScopeUtils      # 数据权限工具
└── SecurityUtils       # 安全工具（获取当前用户）

common-web（依赖 common-security）
├── GlobalExceptionHandler  # 全局异常处理
├── PermissionAspect    # 权限切面
├── OperationLogAspect  # 操作日志切面
├── RateLimitAspect     # 限流切面
├── JacksonConfig       # Long→String 避免 JS 精度丢失
├── CorsConfig          # 跨域配置
└── MybatisPlusConfig   # MyBatis Plus 配置

common-ai（依赖 common-core）
├── AiModelConfig       # AI 模型配置
├── ChatModelFactory    # ChatModel 工厂
└── AiService           # 统一 AI 服务接口
```

### 依赖关系

```
common-core
    ↑
common-security  ←── common-ai
    ↑
common-web
    ↑
所有业务服务（blog-service, system-service, ...）
```

### 思考题
1. 为什么 `common-security` 不依赖 `common-web`？
2. `StpInterfaceImpl` 放在 common-security 而不是 auth-service 的原因是什么？
3. 如果要新增一个「邮件发送」通用功能，应该放在哪个 common 模块？

---

## 十一、容器化与部署

### Docker Compose 部署结构

```
docker-compose.yml
├── PostgreSQL    (5432)  ← 所有服务共用一个数据库实例
├── Redis         (6379)  ← Sa-Token 会话存储
├── Nacos         (8848)  ← 服务注册与发现
├── Elasticsearch (9200)  ← 全文搜索
├── Kafka         (9092)  ← 消息队列
├── MinIO         (9000)  ← 对象存储
├── gateway-service  (1080)
├── auth-service     (1081)
├── system-service   (1082)
├── blog-service     (1083)
├── ai-service       (1086)
├── search-service   (1085)
├── project-service  (1088)
├── mcp-service      (1089)
└── agent-service    (1090)
```

### 思考题
1. 为什么所有服务共用一个 PostgreSQL 实例？生产环境应该怎么部署？
2. Redis 在本系统中有几个用途？
3. 如果要水平扩展 blog-service 到 3 个实例，需要改什么？

---

## 十二、常见微服务挑战与本系统的应对

### 12.1 分布式事务

**问题**：跨服务操作如何保证一致性？

**本系统方案**：
- 同一服务内：`@Transactional` 本地事务
- 跨服务：最终一致性（Kafka 事件 + 重试）
- 示例：文章发布 → 本地更新状态 → afterCommit 发 Kafka → 搜索服务异步索引

### 12.2 跨服务数据查询

**问题**：查询需要多个服务的数据怎么办？

**本系统方案**：
- Feign 同步调用 + 批量优化
- 示例：`fillArticleVOBatch` 批量查询用户信息，避免 N+1

### 12.3 服务雪崩

**问题**：一个服务挂了，调用方怎么办？

**本系统方案**：
- `fail-fast: false`：Nacos 连接失败不阻塞启动
- Feign 调用 try-catch：获取用户信息失败时返回默认值
- Kafka 异步解耦：搜索服务挂了不影响文章发布

### 12.4 雪花 ID 精度丢失

**问题**：Java Long 雪花 ID（18-19位）超过 JS Number.MAX_SAFE_INTEGER

**本系统方案**：
- `SmartLongSerializer`：超过安全范围的 Long 序列化为字符串
- 前端：ID 全部用 `string` 类型，不做 `Number()` 转换

---

## 十三、学习路径建议

### 阶段一：理解架构（当前阶段）
- [x] 理解服务拆分和目录结构
- [x] 理解网关路由和请求流转
- [x] 理解服务间通信（Feign + Kafka）
- [x] 理解认证授权（Sa-Token + RBAC）

### 阶段二：动手实践
- [ ] 新增一个微服务（如 notification-service）
- [ ] 实现 Feign 跨服务调用
- [ ] 实现 Kafka 事件驱动
- [ ] 配置网关路由和权限

### 阶段三：深入优化
- [ ] 分布式事务处理（Saga 模式）
- [ ] 服务熔断和降级（Sentinel/Resilience4j）
- [ ] 链路追踪（SkyWalking/Micrometer Tracing）
- [ ] 配置中心（Nacos Config）

---

## 附录：关键代码文件索引

| 概念 | 文件路径 |
|------|---------|
| 网关路由 | `services/gateway-service/src/main/resources/application.yml` |
| Feign 客户端 | `services/blog-service/.../client/UserServiceClient.java` |
| Kafka 生产者 | `services/blog-service/.../event/ArticleEventPublisher.java` |
| Kafka 消费者 | `services/search-service/.../listener/ArticleEventListener.java` |
| 权限注解 | `common/common-security/.../annotation/RequirePermission.java` |
| 权限切面 | `common/common-web/.../aspect/PermissionAspect.java` |
| Sa-Token 配置 | `common/common-security/.../config/SaTokenConfig.java` |
| 统一响应 | `common/common-core/.../result/Result.java` |
| 全局异常 | `common/common-web/.../handler/GlobalExceptionHandler.java` |
| 数据权限 | `common/common-security/.../util/DataScopeUtils.java` |
| 操作日志 | `common/common-core/.../annotation/OperationLog.java` |
| 雪花ID序列化 | `common/common-web/.../serializer/SmartLongSerializer.java` |
