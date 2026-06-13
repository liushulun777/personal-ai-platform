# ROADMAP - Personal AI Platform

## 开发路线图

### Phase 1：博客系统

| Step | 模块 | 状态 | 说明 |
|------|------|------|------|
| Step1 | 基础工程 | ✅ 完成 | Maven多模块骨架、common模块、微服务初始化、SQL、前端初始化 |
| Step2 | 用户模块 | ✅ 完成 | 用户注册、登录、用户管理、Sa-Token认证 |
| Step3 | 权限模块 | ✅ 完成 | RBAC权限模型、角色管理、菜单管理、数据权限 |
| Step4 | 博客模块 | ✅ 完成 | 文章CRUD、分类管理、标签管理、评论、点赞、收藏 |
| Step5 | 文件模块 | ✅ 完成 | MinIO文件上传下载、图片处理 |
| Step6 | 搜索模块 | ✅ 完成 | ElasticSearch全文搜索、Kafka事件驱动 |
| Step7 | AI模块 | ✅ 完成 | AI摘要、AI标签生成、AI标题生成 |
| Step8 | Docker部署 | ✅ 完成 | Docker Compose编排 |
| Step9 | Kubernetes部署 | ✅ 完成 | K8s部署配置 |

### Phase 2：知识库

- 文档管理
- 向量存储
- RAG检索增强生成

### Phase 3：项目管理

- 项目管理
- 任务管理
- Bug追踪

### Phase 4：AI助手

- 对话管理
- 提示词模板
- 多模型支持

### Phase 5：MCP平台

- MCP工具注册
- 工具调用管理

### Phase 6：Agent工作流

- 工作流编排
- Agent调度
