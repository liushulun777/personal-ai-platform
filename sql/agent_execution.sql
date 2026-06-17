-- Agent 执行记录表
CREATE TABLE IF NOT EXISTS agent_execution (
    id              BIGSERIAL PRIMARY KEY,
    task_id         BIGINT NOT NULL,
    project_id      BIGINT,
    status          SMALLINT NOT NULL DEFAULT 0 COMMENT '执行状态：0-待执行 1-执行中 2-成功 3-失败 4-超时 5-取消',
    executor_id     VARCHAR(100) COMMENT '执行器实例 ID',
    start_time      TIMESTAMP,
    end_time        TIMESTAMP,
    duration        BIGINT COMMENT '执行耗时（毫秒）',
    error_message   TEXT COMMENT '错误信息',
    result_summary  VARCHAR(500) COMMENT '结果摘要',
    create_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_agent_execution_task_id ON agent_execution(task_id);
CREATE INDEX idx_agent_execution_project_id ON agent_execution(project_id);
CREATE INDEX idx_agent_execution_status ON agent_execution(status);
CREATE INDEX idx_agent_execution_create_time ON agent_execution(create_time);

-- 执行步骤表
CREATE TABLE IF NOT EXISTS agent_execution_step (
    id              BIGSERIAL PRIMARY KEY,
    execution_id    BIGINT NOT NULL,
    step_name       VARCHAR(100) NOT NULL COMMENT '步骤名称',
    step_order      INT NOT NULL DEFAULT 0 COMMENT '步骤序号',
    status          SMALLINT NOT NULL DEFAULT 0 COMMENT '执行状态：0-待执行 1-执行中 2-成功 3-失败 4-跳过',
    start_time      TIMESTAMP,
    end_time        TIMESTAMP,
    duration        BIGINT COMMENT '执行耗时（毫秒）',
    detail          TEXT COMMENT '详细信息',
    error_message   TEXT COMMENT '错误信息',
    create_time     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 索引
CREATE INDEX idx_execution_step_execution_id ON agent_execution_step(execution_id);

-- 注释
COMMENT ON TABLE agent_execution IS 'Agent 执行记录表';
COMMENT ON TABLE agent_execution_step IS 'Agent 执行步骤表';
