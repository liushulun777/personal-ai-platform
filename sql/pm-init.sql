-- ============================================================
-- Project Service 升级 SQL 脚本
-- 执行此脚本以初始化或升级项目管理模块的表结构
-- ============================================================

-- ============================================================
-- 1. 修改 pm_task 表（添加新字段）
-- ============================================================

-- 添加父任务ID字段（子任务支持）
ALTER TABLE pm_task ADD COLUMN IF NOT EXISTS parent_task_id BIGINT;

-- 添加来源类型字段
ALTER TABLE pm_task ADD COLUMN IF NOT EXISTS source_type VARCHAR(20) NOT NULL DEFAULT 'MANUAL';

-- 添加阻塞原因字段
ALTER TABLE pm_task ADD COLUMN IF NOT EXISTS blocked_reason TEXT;

-- 添加索引
CREATE INDEX IF NOT EXISTS idx_task_parent ON pm_task(parent_task_id);

-- 更新注释
COMMENT ON COLUMN pm_task.parent_task_id IS '父任务ID（子任务支持）';
COMMENT ON COLUMN pm_task.source_type IS '来源: MANUAL-人工创建, AI_GENERATED-AI拆解生成, AGENT_CREATED-Agent自动创建';
COMMENT ON COLUMN pm_task.blocked_reason IS '阻塞原因';

-- 更新状态注释（新状态机：0-BACKLOG, 1-READY, 2-DOING, 3-REVIEW, 4-DONE, 5-BLOCKED）
COMMENT ON COLUMN pm_task.status IS '状态: 0-BACKLOG, 1-READY, 2-DOING, 3-REVIEW, 4-DONE, 5-BLOCKED';
COMMENT ON COLUMN pm_task.priority IS '优先级: 0-LOW, 1-MEDIUM, 2-HIGH, 3-URGENT';

-- ============================================================
-- 2. 创建 pm_task_execution 表（任务执行记录）
-- ============================================================

CREATE TABLE IF NOT EXISTS pm_task_execution (
    id              BIGINT          PRIMARY KEY,
    task_id         BIGINT          NOT NULL,
    executor_type   VARCHAR(20),
    executor_id     BIGINT,
    action          VARCHAR(50)     NOT NULL,
    content         TEXT,
    prompt          TEXT,
    result          TEXT,
    status          SMALLINT        NOT NULL DEFAULT 0,
    error_msg       TEXT,
    duration        BIGINT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_execution_task ON pm_task_execution(task_id);
CREATE INDEX IF NOT EXISTS idx_execution_executor ON pm_task_execution(executor_id);
CREATE INDEX IF NOT EXISTS idx_execution_action ON pm_task_execution(action);

COMMENT ON TABLE pm_task_execution IS '任务执行记录表';
COMMENT ON COLUMN pm_task_execution.task_id IS '任务ID';
COMMENT ON COLUMN pm_task_execution.executor_type IS '执行者类型: AI, HUMAN, AGENT';
COMMENT ON COLUMN pm_task_execution.executor_id IS '执行者ID';
COMMENT ON COLUMN pm_task_execution.action IS '动作: CREATE, START, UPDATE, COMPLETE, COMMENT, FAIL, BLOCK, ASSIGN, REVIEW';
COMMENT ON COLUMN pm_task_execution.content IS '执行内容';
COMMENT ON COLUMN pm_task_execution.prompt IS 'AI执行的Prompt';
COMMENT ON COLUMN pm_task_execution.result IS 'AI执行的结果';
COMMENT ON COLUMN pm_task_execution.status IS '状态: 0-成功, 1-失败';
COMMENT ON COLUMN pm_task_execution.error_msg IS '错误信息';
COMMENT ON COLUMN pm_task_execution.duration IS '执行耗时(毫秒)';

-- ============================================================
-- 3. 创建 pm_task_comment 表（任务评论）
-- ============================================================

CREATE TABLE IF NOT EXISTS pm_task_comment (
    id              BIGINT          PRIMARY KEY,
    task_id         BIGINT          NOT NULL,
    user_id         BIGINT          NOT NULL,
    content         TEXT            NOT NULL,
    is_ai_summary   SMALLINT        NOT NULL DEFAULT 0,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_comment_task ON pm_task_comment(task_id);

COMMENT ON TABLE pm_task_comment IS '任务评论表';
COMMENT ON COLUMN pm_task_comment.task_id IS '任务ID';
COMMENT ON COLUMN pm_task_comment.user_id IS '评论者ID';
COMMENT ON COLUMN pm_task_comment.content IS '评论内容';
COMMENT ON COLUMN pm_task_comment.is_ai_summary IS '是否AI总结: 0-否, 1-是';

-- ============================================================
-- 4. 创建 pm_task_dependency 表（任务依赖关系）
-- ============================================================

CREATE TABLE IF NOT EXISTS pm_task_dependency (
    id              BIGINT          PRIMARY KEY,
    task_id         BIGINT          NOT NULL,
    dependency_task_id BIGINT       NOT NULL,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_task_dependency ON pm_task_dependency(task_id, dependency_task_id);

COMMENT ON TABLE pm_task_dependency IS '任务依赖关系表';
COMMENT ON COLUMN pm_task_dependency.task_id IS '任务ID';
COMMENT ON COLUMN pm_task_dependency.dependency_task_id IS '依赖的任务ID';

-- ============================================================
-- 完成
-- ============================================================

SELECT 'Project Service 升级 SQL 执行完成！' AS message;
