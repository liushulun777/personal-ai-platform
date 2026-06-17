# Personal AI Platform - 项目架构文档

## 1. 项目概览

**Personal AI Platform** 是一个基于 Spring Cloud 微服务架构的个人 AI 平台，包含博客系统、AI 对话、知识库 RAG、项目管理、MCP 服务管理等模块。

### 核心特性

- 🤖 **AI 驱动**：集成 Spring AI，支持多模型（mimo、OpenAI），实现 RAG 检索、任务拆解、代码生成
- 📝 **博客系统**：完整的文章管理、分类标签、评论点赞收藏
- 📚 **知识库**：文档上传、文本分块、向量嵌入、语义检索
- 📊 **项目管理**：任务状态机、AI 任务拆解、执行追踪
- 🔌 **MCP 平台**：MCP Server 注册、Tool/Resource 管理

---

## 2. 技术栈

### 后端

| 类别 | 技术 | 版本 |
|------|------|------|
| JDK | Eclipse Temurin | 21 |
| 框架 | Spring Boot | 3.4.1 |
| 微服务 | Spring Cloud | 2024.0.0 |
| 微服务(阿里) | Spring Cloud Alibaba | 2023.0.3.2 |
| AI 框架 | Spring AI | 1.0.0-M6 |
| 网关 | Spring Cloud Gateway | - |
| ORM | MyBatis-Plus | 3.5.9 |
| 数据库 | PostgreSQL + pgvector | 17 |
| 连接池 | Druid | 1.2.23 |
| 缓存 | Redis | 8 |
| 消息队列 | Kafka | 7.9.1 |
| 搜索引擎 | ElasticSearch | 8.17.0 |
| 对象存储 | MinIO | - |
| 注册中心 | Nacos | v2.4.3 |
| 认证 | Sa-Token | 1.39.0 |
| API 文档 | Knife4j (OpenAPI3) | 4.5.0 |
| 对象映射 | MapStruct | 1.6.3 |

### 前端

| 类别 | 技术 |
|------|------|
| 框架 | Vue 3.5 |
| 构建工具 | Vite 6 |
| UI 组件库 | Naive UI |
| 状态管理 | Pinia |
| 路由 | Vue Router 4 |
| HTTP 客户端 | Axios |
| CSS 框架 | Tailwind CSS 3 |
| 类型系统 | TypeScript 5.7 |

### 基础设施

| 组件 | 版本 |
|------|------|
| PostgreSQL | pgvector/pgvector:pg17 |
| Redis | redis:8-alpine |
| MinIO | minio/minio:RELEASE.250907 |
| Nacos | nacos/nacos-server:v2.4.3 |
| ElasticSearch | elasticsearch:8.17.0 |
| Kafka | confluentinc/cp-kafka:7.9.1 |

---

## 3. 整体架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                          前端                                    │
│   platform-web (管理后台, Vue3+NaiveUI)  blog-web (公开博客)     │
└───────────────────────────┬─────────────────────────────────────┘
                            │
                    ┌───────▼───────┐
                    │ gateway-service│  (Spring Cloud Gateway, :1080)
                    │   统一路由     │
                    └───────┬───────┘
                            │
    ┌───────────┬───────────┼───────────┬───────────┐
    │           │           │           │           │
┌───▼───┐  ┌───▼───┐  ┌───▼───┐  ┌───▼───┐  ┌───▼───┐
│ auth  │  │system │  │ blog  │  │ file  │  │search │
│:1081  │  │:1082  │  │:1083  │  │:1084  │  │:1085  │
└───────┘  └───────┘  └───┬───┘  └───────┘  └───┬───┘
                         │                     │
                    ┌────▼────┐           ┌────▼────┐
                    │  Kafka  │──────────▶│ES+pgvec│
                    └─────────┘           └─────────┘

    ┌───────────┐  ┌───────────┐  ┌───────────┐
    │    ai     │  │knowledge  │  │  project  │
    │   :1086   │  │   :1087   │  │   :1088   │
    └───────────┘  └───────────┘  └───────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      共享基础设施                                │
│  PostgreSQL(pgvector) │ Redis │ Nacos │ MinIO │ ES │ Kafka     │
└─────────────────────────────────────────────────────────────────┘
```

---

## 4. 微服务列表

| 服务 | 端口 | 职责 | 关键依赖 |
|------|------|------|----------|
| gateway-service | 1080 | API 网关、统一路由 | Nacos, Spring Cloud Gateway |
| auth-service | 1081 | 认证授权、Token 管理 | Sa-Token, Redis |
| system-service | 1082 | 用户/角色/菜单/字典管理 | - |
| blog-service | 1083 | 文章、分类、标签、评论 | Kafka (生产者) |
| file-service | 1084 | 文件上传下载 | MinIO |
| search-service | 1085 | 全文搜索、向量检索 | ES, pgvector, Kafka (消费者) |
| ai-service | 1086 | AI 对话、Prompt 模板 | Spring AI |
| knowledge-service | 1087 | 知识库、RAG 检索 | pgvector, Spring AI |
| project-service | 1088 | 项目/任务/Bug 管理 | - |
| mcp-service | 1089 | MCP Server/Tool 管理 | - |

### 网关路由规则

```
/api/auth/**       → auth-service:1081
/api/system/**     → system-service:1082
/api/blog/**       → blog-service:1083
/api/file/**       → file-service:1084
/api/search/**     → search-service:1085
/api/ai/**         → ai-service:1086
/api/knowledge/**  → knowledge-service:1087
/api/project/**    → project-service:1088
/api/mcp/**        → mcp-service:1089
```

---

## 5. 跨服务通信

### Kafka 事件流

```
blog-service (生产者)
    │
    ├── article-publish ──▶ search-service (索引到 ES)
    ├── article-update  ──▶ search-service (更新 ES)
    └── article-delete  ──▶ search-service (删除 ES 索引)
```

### 数据库

所有服务共享同一个 PostgreSQL 数据库 `platform`，使用不同表前缀：

| 模块 | 表前缀 | 示例 |
|------|--------|------|
| 系统 | sys_ | sys_user, sys_role, sys_menu |
| 博客 | biz_ | biz_article, biz_tag, biz_comment |
| AI | ai_ | ai_conversation, ai_message |
| 知识库 | kb_ | kb_document, kb_chunk, kb_embedding |
| 项目 | pm_ | pm_project, pm_task, pm_bug |
| MCP | mcp_ | mcp_server, mcp_tool, mcp_resource |

---

## 6. 关键技术方案

### 6.1 向量检索 (pgvector)

```sql
-- 知识库向量嵌入表
CREATE TABLE kb_embedding (
    id          BIGINT PRIMARY KEY,
    chunk_id    BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    embedding   vector(768),  -- nomic-embed-text 维度
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 余弦相似度搜索
SELECT id, chunk_id, document_id,
       (1 - (embedding <=> $1::vector)) as similarity
FROM kb_embedding
ORDER BY embedding <=> $1::vector
LIMIT $2;
```

### 6.2 文章分块与向量化

```
文章发布 → Kafka → search-service
    │
    ├── 分块 (ChunkingService)
    ├── 向量嵌入 (EmbeddingService)
    └── 存储到 article_chunks (pgvector)
```

### 6.3 分类模型

支持两种模型格式：
- **SAM_MCE**：EfficientNet-B1 state_dict，Resize(300,300)
- **CNB**：完整 ResNet Module，Resize(256)+CenterCrop(224)

双模型投票合并，置信度 = 加权投票占比。

### 6.4 任务状态机

```
待办 → 进行中 → 已完成 → 已关闭
                    ↓
                 已归档
```

---

## 7. 目录结构

```
personal-ai-platform/
├── pom.xml                    # 父 POM
├── docker-compose.yml         # 容器编排
├── sql/init.sql               # 数据库初始化
├── common/                    # 公共模块
│   ├── common-core/           # BaseEntity, PageQuery, Result
│   ├── common-web/            # CORS, Jackson, MybatisPlus
│   ├── common-security/       # Sa-Token
│   └── common-ai/             # AiModelConfig, EmbeddingModel
├── services/                  # 微服务
│   ├── gateway-service/
│   ├── auth-service/
│   ├── system-service/
│   ├── blog-service/
│   ├── file-service/
│   ├── search-service/
│   ├── ai-service/
│   ├── knowledge-service/
│   ├── project-service/
│   └── mcp-service/
├── frontend/                  # 前端
├── k8s/                       # K8s 部署
└── docs/                      # 文档
```

---

## 8. 部署

### Docker Compose

```bash
# 启动所有服务
docker-compose up -d

# 查看状态
docker-compose ps

# 查看日志
docker-compose logs -f backend
```

### 环境变量

关键配置项（在各服务的 application.yml 中）：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/platform
  data:
    redis:
      host: localhost
      port: 6379
  kafka:
    bootstrap-servers: localhost:29092

ai:
  models:
    embedding-provider: ollama
    config:
      ollama:
        base-url: http://localhost:11434
        model: nomic-embed-text
```
