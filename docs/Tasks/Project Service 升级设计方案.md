# AI Platform - Project Service 升级设计方案

## 1. 需求概述

将基础 CRUD 项目管理系统升级为 **AI驱动的任务执行系统（Agent Work System）**。

### 核心能力

- 任务可被AI自动拆解
- 任务可被Agent自动执行
- 任务状态可自动流转
- 执行过程可追踪、可回放
- 支持AI生成任务 + 人工任务混合管理

---

## 2. Phase 1：任务系统升级

### 2.1 任务状态机升级

**状态定义：**

| 状态码 | 状态名 | 说明 |
|--------|--------|------|
| 0 | BACKLOG | 待规划 |
| 1 | READY | 可执行 |
| 2 | DOING | 执行中 |
| 3 | REVIEW | 待审核 |
| 4 | DONE | 已完成 |
| 5 | BLOCKED | 阻塞 |

**状态流转规则：**

```
BACKLOG(0) → READY(1) → DOING(2) → REVIEW(3) → DONE(4)
                |                      |
                v                      v
            BLOCKED(5) ←─────────── BLOCKED(5)
```

**合法状态转换：**
- BACKLOG → READY, DOING
- READY → DOING, BLOCKED
- DOING → REVIEW, DONE, BLOCKED
- REVIEW → DONE, DOING
- BLOCKED → DOING
- DONE → 无（终态）

### 2.2 任务来源标识

新增字段 `source_type VARCHAR(20)`：

| 值 | 说明 |
|----|------|
| MANUAL | 人工创建 |
| AI_GENERATED | AI拆解生成 |
| AGENT_CREATED | Agent自动创建 |

### 2.3 优先级规范化

| 值 | 优先级 |
|----|--------|
| 0 | LOW |
| 1 | MEDIUM |
| 2 | HIGH |
| 3 | URGENT |

---

## 3. Phase 2：AI执行追踪系统

### 3.1 任务执行记录表

```sql
CREATE TABLE pm_task_execution (
    id              BIGINT PRIMARY KEY,
    task_id         BIGINT NOT NULL,
    executor_type   VARCHAR(20),   -- AI / HUMAN / AGENT
    executor_id     BIGINT,
    action          VARCHAR(50),   -- CREATE / START / UPDATE / COMPLETE / COMMENT / FAIL / BLOCK / ASSIGN / REVIEW
    content         TEXT,
    prompt          TEXT,
    result          TEXT,
    status          SMALLINT DEFAULT 0,
    error_msg       TEXT,
    duration        BIGINT,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 3.2 要求

- 所有任务状态变更必须写入 execution 表
- AI执行必须记录 prompt / result
- Agent执行必须记录调用过程

---

## 4. Phase 3：任务流转能力

### 4.1 任务流转接口

| 接口 | 说明 |
|------|------|
| POST /tasks/{id}/start | 开始任务 (BACKLOG/READY → DOING) |
| POST /tasks/{id}/complete | 完成任务 (DOING/REVIEW → DONE) |
| POST /tasks/{id}/assign | 分配任务 |
| POST /tasks/{id}/block | 阻塞任务 (DOING → BLOCKED) |
| POST /tasks/{id}/move | 移动任务状态 |
| POST /tasks/{id}/submit-review | 提交审核 (DOING → REVIEW) |
| POST /tasks/{id}/approve | 审核通过 (REVIEW → DONE) |
| POST /tasks/{id}/reject | 审核拒绝 (REVIEW → DOING) |
| POST /tasks/{id}/unblock | 解除阻塞 (BLOCKED → DOING) |

### 4.2 流转规则

- DOING → DONE 必须记录执行日志
- BLOCKED 必须记录原因
- REVIEW 必须支持人工或AI审核

---

## 5. Phase 4：AI任务拆解能力

### 5.1 接口

| 接口 | 说明 |
|------|------|
| POST /ai/tasks/decompose | AI任务拆解（仅返回结果） |
| POST /ai/tasks/decompose-and-create | AI任务拆解并自动创建任务 |

### 5.2 输入

```json
{
  "projectId": 1,
  "content": "实现一个博客列表接口"
}
```

### 5.3 输出

```json
[
  {
    "title": "设计数据库表结构",
    "priority": 2,
    "description": "设计博客列表相关的数据库表结构"
  },
  {
    "title": "编写Entity",
    "priority": 2
  },
  {
    "title": "实现Mapper",
    "priority": 1
  },
  {
    "title": "实现Service",
    "priority": 2
  },
  {
    "title": "实现Controller接口",
    "priority": 2
  }
]
```

### 5.4 要求

- 自动写入 pm_task
- source_type = AI_GENERATED
- 自动绑定 project_id

---

## 6. Phase 7：基础能力增强

### 6.1 任务评论系统

**表结构：**

```sql
CREATE TABLE pm_task_comment (
    id              BIGINT PRIMARY KEY,
    task_id         BIGINT NOT NULL,
    user_id         BIGINT NOT NULL,
    content         TEXT NOT NULL,
    is_ai_summary   SMALLINT DEFAULT 0,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted         INT DEFAULT 0
);
```

**接口：**

| 接口 | 说明 |
|------|------|
| POST /task-comments | 创建评论 |
| GET /task-comments/task/{taskId} | 获取任务评论列表 |
| DELETE /task-comments/{id} | 删除评论 |

### 6.2 任务依赖关系

**表结构：**

```sql
CREATE TABLE pm_task_dependency (
    id              BIGINT PRIMARY KEY,
    task_id         BIGINT NOT NULL,
    dependency_task_id BIGINT NOT NULL,
    create_time     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 6.3 子任务支持

在 pm_task 表中新增 `parent_task_id` 字段，支持任务层级关系。

---

## 7. 新增文件清单

### 7.1 实体类

| 文件 | 说明 |
|------|------|
| `domain/entity/TaskExecution.java` | 任务执行记录实体 |
| `domain/entity/TaskComment.java` | 任务评论实体 |
| `domain/entity/TaskDependency.java` | 任务依赖关系实体 |

### 7.2 Mapper

| 文件 | 说明 |
|------|------|
| `mapper/TaskExecutionMapper.java` | 任务执行记录Mapper |
| `mapper/TaskCommentMapper.java` | 任务评论Mapper |
| `mapper/TaskDependencyMapper.java` | 任务依赖关系Mapper |

### 7.3 DTO

| 文件 | 说明 |
|------|------|
| `domain/dto/TaskExecutionCreateDTO.java` | 执行记录创建DTO |
| `domain/dto/TaskCommentCreateDTO.java` | 评论创建DTO |
| `domain/dto/AiTaskDecomposeDTO.java` | AI任务拆解请求DTO |

### 7.4 VO

| 文件 | 说明 |
|------|------|
| `domain/vo/TaskExecutionVO.java` | 执行记录VO |
| `domain/vo/TaskCommentVO.java` | 评论VO |
| `domain/vo/AiTaskDecomposeVO.java` | AI任务拆解结果VO |

### 7.5 Convert

| 文件 | 说明 |
|------|------|
| `convert/TaskExecutionConvert.java` | 执行记录转换器 |
| `convert/TaskCommentConvert.java` | 评论转换器 |

### 7.6 Service

| 文件 | 说明 |
|------|------|
| `service/TaskExecutionService.java` | 执行记录服务接口 |
| `service/TaskCommentService.java` | 评论服务接口 |
| `service/AiTaskService.java` | AI任务服务接口 |
| `service/impl/TaskExecutionServiceImpl.java` | 执行记录服务实现 |
| `service/impl/TaskCommentServiceImpl.java` | 评论服务实现 |
| `service/impl/AiTaskServiceImpl.java` | AI任务服务实现 |

### 7.7 Controller

| 文件 | 说明 |
|------|------|
| `controller/AiTaskController.java` | AI任务控制器 |
| `controller/TaskCommentController.java` | 评论控制器 |

---

## 8. 修改文件清单

| 文件 | 修改内容 |
|------|----------|
| `sql/init.sql` | 添加新表、修改pm_task表结构 |
| `pom.xml` | 添加common-ai依赖 |
| `application.yml` | 添加AI模型配置 |
| `domain/entity/Task.java` | 添加新字段 |
| `domain/dto/TaskCreateDTO.java` | 添加新字段 |
| `domain/dto/TaskUpdateDTO.java` | 添加新字段 |
| `domain/dto/TaskQueryDTO.java` | 添加sourceType查询 |
| `domain/vo/TaskVO.java` | 添加新字段 |
| `service/TaskService.java` | 添加流转方法 |
| `service/impl/TaskServiceImpl.java` | 实现流转逻辑 |
| `controller/TaskController.java` | 添加流转接口 |

---

## 9. API 接口汇总

### 9.1 任务管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /tasks | 分页查询任务 |
| GET | /tasks/{id} | 获取任务详情 |
| POST | /tasks | 创建任务 |
| PUT | /tasks | 更新任务 |
| DELETE | /tasks/{id} | 删除任务 |

### 9.2 任务流转

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /tasks/{id}/start | 开始任务 |
| POST | /tasks/{id}/complete | 完成任务 |
| POST | /tasks/{id}/assign | 分配任务 |
| POST | /tasks/{id}/block | 阻塞任务 |
| POST | /tasks/{id}/move | 移动任务状态 |
| POST | /tasks/{id}/submit-review | 提交审核 |
| POST | /tasks/{id}/approve | 审核通过 |
| POST | /tasks/{id}/reject | 审核拒绝 |
| POST | /tasks/{id}/unblock | 解除阻塞 |
| GET | /tasks/{id}/executions | 获取执行记录 |

### 9.3 AI任务

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /ai/tasks/decompose | AI任务拆解 |
| POST | /ai/tasks/decompose-and-create | AI任务拆解并创建 |

### 9.4 任务评论

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /task-comments | 创建评论 |
| GET | /task-comments/task/{taskId} | 获取任务评论列表 |
| DELETE | /task-comments/{id} | 删除评论 |

---

## 10. 测试说明

### 10.1 任务流转测试

```bash
# 创建任务
curl -X POST http://localhost:1088/tasks -H "Content-Type: application/json" -d '{"projectId":1,"title":"测试任务"}'

# 开始任务
curl -X POST http://localhost:1088/tasks/{id}/start

# 提交审核
curl -X POST http://localhost:1088/tasks/{id}/submit-review

# 审核通过
curl -X POST http://localhost:1088/tasks/{id}/approve
```

### 10.2 AI任务拆解测试

```bash
# AI任务拆解
curl -X POST http://localhost:1088/ai/tasks/decompose -H "Content-Type: application/json" -d '{"projectId":1,"content":"实现一个博客列表接口"}'

# AI任务拆解并创建
curl -X POST http://localhost:1088/ai/tasks/decompose-and-create -H "Content-Type: application/json" -d '{"projectId":1,"content":"实现一个博客列表接口"}'
```

### 10.3 执行记录查询测试

```bash
# 获取任务执行记录
curl http://localhost:1088/tasks/{id}/executions
```

---

## 11. 注意事项

1. **状态机验证**：所有状态流转都经过合法性验证，不允许非法状态转换
2. **执行日志**：所有状态变更都会自动记录到 pm_task_execution 表
3. **阻塞原因**：阻塞任务时必须提供原因
4. **AI拆解**：AI拆解的任务自动设置 source_type = AI_GENERATED
5. **子任务**：通过 parent_task_id 支持任务层级关系
6. **任务依赖**：通过 pm_task_dependency 表支持任务依赖关系
