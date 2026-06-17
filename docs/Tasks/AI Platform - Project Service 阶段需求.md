# AI Platform - Project Service 下一阶段需求（AI可执行版）

## 目标

将当前基础 CRUD 项目管理系统升级为：

> AI驱动的任务执行系统（Agent Work System）

实现能力：

- 任务可被AI自动拆解
- 任务可被Agent自动执行
- 任务状态可自动流转
- 执行过程可追踪、可回放
- 支持AI生成任务 + 人工任务混合管理

---

# Phase 1：任务系统升级（核心基础）

## 1.1 任务状态机升级（必须）

### 修改表：pm_task

新增状态定义：

```text
status:
0 - BACKLOG（待规划）
1 - READY（可执行）
2 - DOING（执行中）
3 - REVIEW（待审核）
4 - DONE（已完成）
5 - BLOCKED（阻塞）
```

要求：
替换原有 status 0/1/2/3 设计
所有查询接口适配新状态
支持按状态筛选任务

-------------------------------------

1.2 任务来源标识（必须）
修改表：pm_task

新增字段：

source_type VARCHAR(20)
枚举：
MANUAL        人工创建
AI_GENERATED  AI拆解生成
AGENT_CREATED Agent自动创建
1.3 任务优先级规范化

统一 priority：

0 - LOW
1 - MEDIUM
2 - HIGH
3 - URGENT
Phase 2：AI执行追踪系统（核心能力）
2.1 新增任务执行记录表
CREATE TABLE pm_task_execution (
id BIGINT PRIMARY KEY,
task_id BIGINT NOT NULL,

    executor_type VARCHAR(20),   -- AI / HUMAN / AGENT
    executor_id BIGINT,

    action VARCHAR(50),          -- CREATE / START / UPDATE / COMPLETE / COMMENT / FAIL
    content TEXT,

    status SMALLINT DEFAULT 0,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
2.2 要求
所有任务状态变更必须写入 execution 表
AI执行必须记录 prompt / result
Agent执行必须记录调用过程
Phase 3：任务流转能力（核心逻辑）
3.1 新增任务流转接口
必须实现 API：
POST /task/start/{id}
POST /task/complete/{id}
POST /task/assign/{id}
POST /task/block/{id}
POST /task/move/{id}
3.2 流转规则
BACKLOG → READY → DOING → REVIEW → DONE
↓
BLOCKED
3.3 规则要求
DOING → DONE 必须记录执行日志
BLOCKED 必须记录原因
REVIEW 必须支持人工或AI审核
Phase 4：AI任务拆解能力（关键升级）
4.1 新增接口
POST /ai/task/decompose
4.2 输入
{
"projectId": 1,
"content": "实现一个博客列表接口"
}
4.3 输出（必须）
[
{
"title": "设计数据库表结构",
"priority": 2
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
4.4 要求
自动写入 pm_task
source_type = AI_GENERATED
自动绑定 project_id
Phase 5：Agent执行能力（核心升级）
5.1 新增接口
POST /agent/task/execute/{taskId}
5.2 执行流程

Agent必须执行：

获取任务信息
分析任务内容
调用 MCP 工具
执行操作（代码/数据库/API）
写执行记录
更新任务状态
5.3 MCP调用示例
tool: database.query
tool: file.write
tool: http.request
tool: code.generate
5.4 执行要求
必须记录每一步执行日志
必须支持失败回滚标记
必须支持重试机制
Phase 6：任务自动流转（AI驱动）
6.1 自动状态推进规则
AI执行成功 → DOING → REVIEW
AI审核通过 → REVIEW → DONE
执行失败 → DOING → BLOCKED
6.2 Agent行为要求
能自动推进任务状态
能自动生成子任务
能自动标记阻塞原因
Phase 7：基础能力增强（非核心）
7.1 任务评论系统
支持任务评论
支持AI评论总结
7.2 任务依赖关系

新增字段：

dependency_task_id BIGINT
7.3 子任务支持
parent_task_id BIGINT
非功能要求
必须满足：
所有任务操作必须可追踪
所有AI操作必须可记录
所有状态变更必须可回放
API必须幂等
最终目标

系统最终应具备能力：

AI可以自动拆解需求 → 生成任务 → 执行任务 → 更新状态 → 产出结果

交付标准
Task系统可完整流转
AI可生成任务
Agent可执行任务
执行过程可追踪