# Phase 7：Agent 执行平台

## 一、目标

当前 AI Platform 已具备：

* Project（项目）
* Task（任务）
* AI Service（大模型调用）
* Knowledge Service（知识库/RAG）
* MCP Service（工具调用）

下一阶段需要实现 **Agent 自动执行能力**，使 AI 不仅能够拆分需求，还能够自动完成代码开发、测试、提交等任务。

最终目标：

> AI 从"需求生成器"升级为"软件开发执行者"。

---

# 二、整体架构

```
User
 │
 │ 创建项目
 ▼
Project Service
 │
 │ 保存项目
 ▼
Kafka：ProjectCreatedEvent
 │
 ▼
AI Service（Planner）
 │
 │ 调用 LLM
 │ 拆分任务
 ▼
Project Service
 │
 │ 创建 Task
 ▼
Kafka：TaskCreatedEvent
 │
 ▼
Agent Service（Executor）
 │
 │ 创建 Workspace
 │
 │ 调用 Knowledge
 │
 │ 调用 MCP
 │
 ▼
修改代码
 │
 ▼
编译
 │
 ▼
测试
 │
 ▼
Git Commit
 │
 ▼
更新 Task 状态
```

---

# 三、职责划分

## project-service

负责：

* 项目管理
* 任务管理
* Bug 管理
* 执行日志
* Task 状态管理

不负责：

* AI 推理
* 修改代码
* 调用大模型

---

## ai-service（Planner）

负责：

* 调用 LLM
* Prompt 管理
* 项目需求分析
* 自动拆分 Task
* 识别任务依赖
* 设置优先级
* 输出 JSON Task

不负责：

* 修改代码
* 文件操作
* Git 操作

---

## agent-service（新增）

负责：

* 消费 TaskCreatedEvent
* 执行 Task
* 创建 Workspace
* 调用 MCP
* 更新 Task 状态
* 写执行日志

这是整个系统真正的执行器。

---

## knowledge-service

负责：

* Java 代码检索
* 向量检索
* RAG
* 返回相关源码
* 提供上下文

Agent 不直接搜索代码，而是统一通过 Knowledge Service 获取上下文。

---

## mcp-service

负责统一调用工具。

提供：

* 文件系统
* Git
* Terminal
* Database
* HTTP
* Docker（后续）

Agent 不直接执行命令，而是通过 MCP 调用。

---

# 四、Workspace（工作区）

Agent 不允许直接修改主项目源码。

每一个 Task 都需要创建独立 Workspace。

例如：

```
workspace/

    task-1001/

        AIPlatform/
            services/
            pom.xml
            web/
            docker/
```

流程：

```
Git Clone
      │
      ▼
Workspace
      │
      ▼
AI 修改代码
      │
      ▼
编译
      │
      ▼
测试
      │
      ▼
Git Commit
```

Task 完成后：

Workspace 可删除或保留用于调试。

---

# 五、执行流程

## Step1

用户创建项目

```
POST /project
```

Project Service：

保存 Project。

发送：

```
ProjectCreatedEvent
```

---

## Step2

AI Service：

监听：

```
ProjectCreatedEvent
```

调用 LLM。

输出：

```
Task List
```

例如：

```
数据库设计

实现登录

实现博客

实现评论

实现知识库
```

随后调用：

```
Project Service
```

创建全部 Task。

---

## Step3

Project Service：

发送：

```
TaskCreatedEvent
```

---

## Step4

Agent Service：

消费：

```
TaskCreatedEvent
```

创建：

Workspace。

---

## Step5

Agent：

调用：

Knowledge Service。

获取：

* Controller
* Service
* Mapper
* Entity
* SQL
* Vue 页面

作为上下文。

---

## Step6

Agent：

调用：

LLM。

生成代码。

---

## Step7

调用 MCP。

例如：

```
filesystem.write
```

写入：

Java 文件。

继续：

```
terminal.exec

mvn clean package
```

编译。

继续：

```
mvn test
```

执行测试。

---

## Step8

调用：

```
git.commit
```

提交代码。

随后：

更新：

Task：

```
DONE
```

---

# 六、Task 生命周期

```
BACKLOG

↓

READY

↓

RUNNING

↓

REVIEW

↓

DONE
```

失败：

```
RUNNING

↓

FAILED
```

人工驳回：

```
REVIEW

↓

BACKLOG
```

---

# 七、执行日志

新增：

Task Execution Log。

记录：

```
开始执行

↓

检索代码

↓

生成代码

↓

写文件

↓

编译

↓

测试

↓

Git Commit

↓

完成
```

用于前端展示 Agent 工作过程。

---

# 八、目录规划

```
services/

    ai-service

    agent-service（新增）

    auth-service

    blog-service

    file-service

    gateway-service

    knowledge-service

    mcp-service

    project-service

    search-service

    system-service
```

新增：

```
workspace/
```

用于 Agent 工作目录。

---

# 九、开发任务

## 第一阶段

* 新建 agent-service
* Kafka 消费 TaskCreatedEvent
* 创建 Workspace
* Task 状态流转

---

## 第二阶段

* 集成 Knowledge Service
* 获取源码上下文
* Prompt 拼装

---

## 第三阶段

* MCP 文件写入
* Git 操作
* Terminal 执行

---

## 第四阶段

* 编译
* 单元测试
* Task 日志
* 自动 Commit

---

## 第五阶段

支持：

* 多 Agent 并行
* Task 依赖
* 自动重试
* 人工审批
* Pull Request
* Docker 沙箱执行
* GitHub/GitLab 集成

---

# 十、验收标准

满足以下能力：

* 创建项目后自动拆分任务
* 自动生成 Task
* Agent 自动领取任务
* 创建独立 Workspace
* 自动检索代码上下文
* 自动生成代码
* 自动写入文件
* 自动编译
* 自动执行测试
* 自动 Git Commit
* 更新 Task 状态
* 前端可查看完整执行日志

最终形成完整闭环：

```
需求
    ↓
项目
    ↓
任务
    ↓
Agent
    ↓
代码
    ↓
编译
    ↓
测试
    ↓
提交
    ↓
完成
```
