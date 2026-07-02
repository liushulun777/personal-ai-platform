-- 报表平台数据库初始化脚本

-- 数据源表
CREATE TABLE IF NOT EXISTS report_datasource (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    config JSONB NOT NULL,
    status SMALLINT DEFAULT 1,
    remark VARCHAR(500),
    create_by BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE report_datasource IS '数据源表';
COMMENT ON COLUMN report_datasource.name IS '数据源名称';
COMMENT ON COLUMN report_datasource.type IS '数据源类型: MYSQL/POSTGRESQL/API/EXCEL/CSV';
COMMENT ON COLUMN report_datasource.config IS '连接配置 (JSON)';
COMMENT ON COLUMN report_datasource.status IS '状态: 0-禁用 1-启用';
COMMENT ON COLUMN report_datasource.remark IS '备注';

-- 数据集表
CREATE TABLE IF NOT EXISTS report_dataset (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    source_id BIGINT NOT NULL,
    config JSONB NOT NULL,
    fields JSONB,
    status SMALLINT DEFAULT 1,
    remark VARCHAR(500),
    create_by BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE report_dataset IS '数据集表';
COMMENT ON COLUMN report_dataset.name IS '数据集名称';
COMMENT ON COLUMN report_dataset.type IS '数据集类型: SQL/VISUAL/API';
COMMENT ON COLUMN report_dataset.source_id IS '数据源ID';
COMMENT ON COLUMN report_dataset.config IS '配置信息 (JSON)';
COMMENT ON COLUMN report_dataset.fields IS '字段列表 (JSON)';
COMMENT ON COLUMN report_dataset.status IS '状态: 0-禁用 1-启用';
COMMENT ON COLUMN report_dataset.remark IS '备注';

-- 报表定义表
CREATE TABLE IF NOT EXISTS report_definition (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    type VARCHAR(20) NOT NULL,
    category VARCHAR(50),
    config JSONB NOT NULL,
    status SMALLINT DEFAULT 0,
    version INTEGER DEFAULT 1,
    remark VARCHAR(500),
    create_by BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE report_definition IS '报表定义表';
COMMENT ON COLUMN report_definition.name IS '报表名称';
COMMENT ON COLUMN report_definition.code IS '报表编码';
COMMENT ON COLUMN report_definition.type IS '报表类型: NORMAL/COMPLEX/DASHBOARD';
COMMENT ON COLUMN report_definition.category IS '报表分类';
COMMENT ON COLUMN report_definition.config IS '报表配置 (JSON)';
COMMENT ON COLUMN report_definition.status IS '状态: 0-草稿 1-已发布 2-已归档';
COMMENT ON COLUMN report_definition.version IS '版本号';
COMMENT ON COLUMN report_definition.remark IS '备注';

-- 大屏表
CREATE TABLE IF NOT EXISTS report_dashboard (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    config JSONB NOT NULL,
    status SMALLINT DEFAULT 0,
    share_url VARCHAR(200),
    remark VARCHAR(500),
    create_by BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE report_dashboard IS '大屏表';
COMMENT ON COLUMN report_dashboard.name IS '大屏名称';
COMMENT ON COLUMN report_dashboard.config IS '大屏配置 (JSON)';
COMMENT ON COLUMN report_dashboard.status IS '状态: 0-草稿 1-已发布 2-已归档';
COMMENT ON COLUMN report_dashboard.share_url IS '分享链接';
COMMENT ON COLUMN report_dashboard.remark IS '备注';

-- 调度任务表
CREATE TABLE IF NOT EXISTS report_schedule (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    report_id BIGINT NOT NULL,
    cron VARCHAR(50) NOT NULL,
    config JSONB NOT NULL,
    status SMALLINT DEFAULT 0,
    last_execute_time TIMESTAMP,
    next_execute_time TIMESTAMP,
    remark VARCHAR(500),
    create_by BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE report_schedule IS '调度任务表';
COMMENT ON COLUMN report_schedule.name IS '任务名称';
COMMENT ON COLUMN report_schedule.report_id IS '报表ID';
COMMENT ON COLUMN report_schedule.cron IS 'Cron表达式';
COMMENT ON COLUMN report_schedule.config IS '执行配置 (JSON)';
COMMENT ON COLUMN report_schedule.status IS '状态: 0-停止 1-运行 2-暂停';
COMMENT ON COLUMN report_schedule.last_execute_time IS '上次执行时间';
COMMENT ON COLUMN report_schedule.next_execute_time IS '下次执行时间';
COMMENT ON COLUMN report_schedule.remark IS '备注';

-- 报表权限表
CREATE TABLE IF NOT EXISTS report_permission (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    role_id BIGINT,
    resource_id BIGINT NOT NULL,
    resource_type VARCHAR(20) NOT NULL,
    permission VARCHAR(20) NOT NULL,
    data_scope JSONB,
    create_by BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_by BIGINT,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE report_permission IS '报表权限表';
COMMENT ON COLUMN report_permission.user_id IS '用户ID';
COMMENT ON COLUMN report_permission.role_id IS '角色ID';
COMMENT ON COLUMN report_permission.resource_id IS '资源ID';
COMMENT ON COLUMN report_permission.resource_type IS '资源类型: DATASOURCE/DATASET/REPORT/DASHBOARD';
COMMENT ON COLUMN report_permission.permission IS '权限类型: VIEW/EDIT/DELETE/EXPORT/PRINT';
COMMENT ON COLUMN report_permission.data_scope IS '数据范围 (JSON)';

-- 操作日志表
CREATE TABLE IF NOT EXISTS report_log (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    resource_id BIGINT,
    resource_type VARCHAR(20),
    action VARCHAR(20),
    detail JSONB,
    ip_address VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE report_log IS '操作日志表';
COMMENT ON COLUMN report_log.user_id IS '用户ID';
COMMENT ON COLUMN report_log.resource_id IS '资源ID';
COMMENT ON COLUMN report_log.resource_type IS '资源类型';
COMMENT ON COLUMN report_log.action IS '操作类型: CREATE/UPDATE/DELETE/VIEW/EXPORT/PRINT';
COMMENT ON COLUMN report_log.detail IS '操作详情 (JSON)';
COMMENT ON COLUMN report_log.ip_address IS 'IP地址';

-- 创建索引
CREATE INDEX idx_report_datasource_type ON report_datasource(type);
CREATE INDEX idx_report_datasource_status ON report_datasource(status);

CREATE INDEX idx_report_dataset_source_id ON report_dataset(source_id);
CREATE INDEX idx_report_dataset_type ON report_dataset(type);

CREATE INDEX idx_report_definition_code ON report_definition(code);
CREATE INDEX idx_report_definition_type ON report_definition(type);
CREATE INDEX idx_report_definition_category ON report_definition(category);
CREATE INDEX idx_report_definition_status ON report_definition(status);

CREATE INDEX idx_report_dashboard_status ON report_dashboard(status);
CREATE INDEX idx_report_dashboard_share_url ON report_dashboard(share_url);

CREATE INDEX idx_report_schedule_report_id ON report_schedule(report_id);
CREATE INDEX idx_report_schedule_status ON report_schedule(status);

CREATE INDEX idx_report_permission_user_id ON report_permission(user_id);
CREATE INDEX idx_report_permission_role_id ON report_permission(role_id);
CREATE INDEX idx_report_permission_resource ON report_permission(resource_id, resource_type);

CREATE INDEX idx_report_log_user_id ON report_log(user_id);
CREATE INDEX idx_report_log_resource ON report_log(resource_id, resource_type);
CREATE INDEX idx_report_log_create_time ON report_log(create_time);
