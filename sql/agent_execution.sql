-- Agent 执行记录表
CREATE TABLE IF NOT EXISTS agent_execution (
    id              BIGSERIAL PRIMARY KEY,
    task_id         BIGINT NOT NULL,
    project_id      BIGINT,
    status          SMALLINT NOT NULL DEFAULT 0,
    executor_id     VARCHAR(100),
    start_time      TIMESTAMP,
    end_time        TIMESTAMP,
    duration        BIGINT,
    error_message   TEXT,
    result_summary  VARCHAR(500),
    create_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_agent_execution_task_id ON agent_execution(task_id);
CREATE INDEX IF NOT EXISTS idx_agent_execution_project_id ON agent_execution(project_id);
CREATE INDEX IF NOT EXISTS idx_agent_execution_status ON agent_execution(status);
CREATE INDEX IF NOT EXISTS idx_agent_execution_create_time ON agent_execution(create_time);

-- 表注释
COMMENT ON TABLE agent_execution IS 'Agent 执行记录表';
COMMENT ON COLUMN agent_execution.status IS '执行状态：0-待执行 1-执行中 2-成功 3-失败 4-超时 5-取消';
COMMENT ON COLUMN agent_execution.executor_id IS '执行器实例 ID';
COMMENT ON COLUMN agent_execution.duration IS '执行耗时（毫秒）';
COMMENT ON COLUMN agent_execution.error_message IS '错误信息';
COMMENT ON COLUMN agent_execution.result_summary IS '结果摘要';

-- 执行步骤表
CREATE TABLE IF NOT EXISTS agent_execution_step (
    id              BIGSERIAL PRIMARY KEY,
    execution_id    BIGINT NOT NULL,
    step_name       VARCHAR(100) NOT NULL,
    step_order      INT NOT NULL DEFAULT 0,
    status          SMALLINT NOT NULL DEFAULT 0,
    start_time      TIMESTAMP,
    end_time        TIMESTAMP,
    duration        BIGINT,
    detail          TEXT,
    error_message   TEXT,
    create_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX IF NOT EXISTS idx_execution_step_execution_id ON agent_execution_step(execution_id);

-- 表注释
COMMENT ON TABLE agent_execution_step IS 'Agent 执行步骤表';
COMMENT ON COLUMN agent_execution_step.step_name IS '步骤名称';
COMMENT ON COLUMN agent_execution_step.step_order IS '步骤序号';
COMMENT ON COLUMN agent_execution_step.status IS '执行状态：0-待执行 1-执行中 2-成功 3-失败 4-跳过';
COMMENT ON COLUMN agent_execution_step.duration IS '执行耗时（毫秒）';
COMMENT ON COLUMN agent_execution_step.detail IS '详细信息';
COMMENT ON COLUMN agent_execution_step.error_message IS '错误信息';
