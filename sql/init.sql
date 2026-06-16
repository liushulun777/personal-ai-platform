-- ============================================================
-- Personal AI Platform - PostgreSQL 初始化脚本
-- 注意: 数据库由 docker-compose POSTGRES_DB=platform 自动创建
-- ============================================================

-- ============================================================
-- 系统表
-- ============================================================

-- 用户表
CREATE TABLE sys_user (
    id              BIGINT          PRIMARY KEY,
    username        VARCHAR(50)     NOT NULL UNIQUE,
    password        VARCHAR(200)    NOT NULL,
    nickname        VARCHAR(50),
    email           VARCHAR(100),
    phone           VARCHAR(20),
    avatar          VARCHAR(500),
    status          INT             NOT NULL DEFAULT 1,
    create_by       BIGINT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       BIGINT,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_user IS '用户表';
COMMENT ON COLUMN sys_user.id IS '用户ID';
COMMENT ON COLUMN sys_user.username IS '用户名';
COMMENT ON COLUMN sys_user.password IS '密码';
COMMENT ON COLUMN sys_user.nickname IS '昵称';
COMMENT ON COLUMN sys_user.email IS '邮箱';
COMMENT ON COLUMN sys_user.phone IS '手机号';
COMMENT ON COLUMN sys_user.avatar IS '头像URL';
COMMENT ON COLUMN sys_user.status IS '状态: 1-启用, 0-禁用';
COMMENT ON COLUMN sys_user.deleted IS '逻辑删除: 0-未删除, 1-已删除';

-- 角色表
CREATE TABLE sys_role (
    id              BIGINT          PRIMARY KEY,
    role_name       VARCHAR(50)     NOT NULL,
    role_key        VARCHAR(50)     NOT NULL UNIQUE,
    description     VARCHAR(200),
    sort            INT             NOT NULL DEFAULT 0,
    status          INT             NOT NULL DEFAULT 1,
    create_by       BIGINT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       BIGINT,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_role IS '角色表';
COMMENT ON COLUMN sys_role.role_name IS '角色名称';
COMMENT ON COLUMN sys_role.role_key IS '角色标识';
COMMENT ON COLUMN sys_role.description IS '描述';
COMMENT ON COLUMN sys_role.sort IS '排序';
COMMENT ON COLUMN sys_role.status IS '状态: 1-启用, 0-禁用';

-- 菜单表
CREATE TABLE sys_menu (
    id              BIGINT          PRIMARY KEY,
    parent_id       BIGINT          NOT NULL DEFAULT 0,
    menu_name       VARCHAR(50)     NOT NULL,
    path            VARCHAR(200),
    component       VARCHAR(200),
    icon            VARCHAR(50),
    menu_type       INT             NOT NULL DEFAULT 0,
    permission      VARCHAR(100),
    sort            INT             NOT NULL DEFAULT 0,
    visible         INT             NOT NULL DEFAULT 1,
    status          INT             NOT NULL DEFAULT 1,
    create_by       BIGINT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       BIGINT,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_menu IS '菜单表';
COMMENT ON COLUMN sys_menu.parent_id IS '父菜单ID';
COMMENT ON COLUMN sys_menu.menu_name IS '菜单名称';
COMMENT ON COLUMN sys_menu.path IS '路由路径';
COMMENT ON COLUMN sys_menu.component IS '组件路径';
COMMENT ON COLUMN sys_menu.icon IS '图标';
COMMENT ON COLUMN sys_menu.menu_type IS '类型: 0-目录, 1-菜单, 2-按钮';
COMMENT ON COLUMN sys_menu.permission IS '权限标识';

-- 用户角色关联表
CREATE TABLE sys_user_role (
    id              BIGINT          PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    role_id         BIGINT          NOT NULL,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_user_role ON sys_user_role(user_id, role_id);

COMMENT ON TABLE sys_user_role IS '用户角色关联表';

-- 角色菜单关联表
CREATE TABLE sys_role_menu (
    id              BIGINT          PRIMARY KEY,
    role_id         BIGINT          NOT NULL,
    menu_id         BIGINT          NOT NULL,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_role_menu ON sys_role_menu(role_id, menu_id);

COMMENT ON TABLE sys_role_menu IS '角色菜单关联表';

-- 字典类型表
CREATE TABLE sys_dict_type (
    id              BIGINT          PRIMARY KEY,
    dict_name       VARCHAR(100)    NOT NULL,
    dict_type       VARCHAR(100)    NOT NULL UNIQUE,
    description     VARCHAR(200),
    status          INT             NOT NULL DEFAULT 1,
    create_by       BIGINT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       BIGINT,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_dict_type IS '字典类型表';

-- 字典数据表
CREATE TABLE sys_dict_data (
    id              BIGINT          PRIMARY KEY,
    dict_type       VARCHAR(100)    NOT NULL,
    dict_label      VARCHAR(100)    NOT NULL,
    dict_value      VARCHAR(100)    NOT NULL,
    sort            INT             NOT NULL DEFAULT 0,
    status          INT             NOT NULL DEFAULT 1,
    remark          VARCHAR(200),
    create_by       BIGINT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       BIGINT,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0
);

COMMENT ON TABLE sys_dict_data IS '字典数据表';

-- 操作日志表
CREATE TABLE sys_log (
    id              BIGINT          PRIMARY KEY,
    module          VARCHAR(50),
    operation       VARCHAR(100),
    method          VARCHAR(200),
    request_url     VARCHAR(500),
    request_method  VARCHAR(10),
    request_params  TEXT,
    response_data   TEXT,
    ip              VARCHAR(50),
    user_agent      VARCHAR(500),
    user_id         BIGINT,
    username        VARCHAR(50),
    status          INT             NOT NULL DEFAULT 1,
    error_msg       TEXT,
    duration        BIGINT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE sys_log IS '操作日志表';

-- ============================================================
-- 业务表 - 博客模块
-- ============================================================

-- 分类表
CREATE TABLE biz_category (
    id              BIGINT          PRIMARY KEY,
    name            VARCHAR(50)     NOT NULL,
    slug            VARCHAR(50)     NOT NULL UNIQUE,
    description     VARCHAR(200),
    sort            INT             NOT NULL DEFAULT 0,
    status          INT             NOT NULL DEFAULT 1,
    create_by       BIGINT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       BIGINT,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0
);

COMMENT ON TABLE biz_category IS '文章分类表';

-- 标签表
CREATE TABLE biz_tag (
    id              BIGINT          PRIMARY KEY,
    name            VARCHAR(50)     NOT NULL,
    slug            VARCHAR(50)     NOT NULL UNIQUE,
    description     VARCHAR(200),
    color           VARCHAR(20),
    sort            INT             NOT NULL DEFAULT 0,
    status          INT             NOT NULL DEFAULT 1,
    create_by       BIGINT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       BIGINT,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0
);

COMMENT ON TABLE biz_tag IS '文章标签表';

-- 文章表
CREATE TABLE biz_article (
    id              BIGINT          PRIMARY KEY,
    title           VARCHAR(200)    NOT NULL,
    summary         VARCHAR(500),
    content         TEXT,
    cover           VARCHAR(500),
    author_id       BIGINT          NOT NULL,
    category_id     BIGINT,
    status          INT             NOT NULL DEFAULT 0,
    view_count      INT             NOT NULL DEFAULT 0,
    like_count      INT             NOT NULL DEFAULT 0,
    favorite_count  INT             NOT NULL DEFAULT 0,
    comment_count   INT             NOT NULL DEFAULT 0,
    is_top          INT             NOT NULL DEFAULT 0,
    is_original     INT             NOT NULL DEFAULT 1,
    source_url      VARCHAR(500),
    publish_time    TIMESTAMP,
    create_by       BIGINT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       BIGINT,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0
);

CREATE INDEX idx_article_author ON biz_article(author_id);
CREATE INDEX idx_article_category ON biz_article(category_id);
CREATE INDEX idx_article_status ON biz_article(status);
CREATE INDEX idx_article_publish_time ON biz_article(publish_time);

COMMENT ON TABLE biz_article IS '文章表';
COMMENT ON COLUMN biz_article.status IS '状态: 0-草稿, 1-已发布, 2-已归档';
COMMENT ON COLUMN biz_article.is_top IS '是否置顶: 0-否, 1-是';
COMMENT ON COLUMN biz_article.is_original IS '是否原创: 0-否, 1-是';

-- 文章标签关联表
CREATE TABLE biz_article_tag (
    id              BIGINT          PRIMARY KEY,
    article_id      BIGINT          NOT NULL,
    tag_id          BIGINT          NOT NULL,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_article_tag ON biz_article_tag(article_id, tag_id);

COMMENT ON TABLE biz_article_tag IS '文章标签关联表';

-- 评论表
CREATE TABLE biz_comment (
    id              BIGINT          PRIMARY KEY,
    article_id      BIGINT          NOT NULL,
    parent_id       BIGINT          NOT NULL DEFAULT 0,
    user_id         BIGINT          NOT NULL,
    content         TEXT            NOT NULL,
    like_count      INT             NOT NULL DEFAULT 0,
    status          INT             NOT NULL DEFAULT 1,
    create_by       BIGINT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       BIGINT,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0
);

CREATE INDEX idx_comment_article ON biz_comment(article_id);
CREATE INDEX idx_comment_user ON biz_comment(user_id);

COMMENT ON TABLE biz_comment IS '评论表';
COMMENT ON COLUMN biz_comment.parent_id IS '父评论ID, 0为顶级评论';

-- 点赞表
CREATE TABLE biz_like (
    id              BIGINT          PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    target_id       BIGINT          NOT NULL,
    target_type     INT             NOT NULL DEFAULT 1,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_like_target ON biz_like(user_id, target_id, target_type);

COMMENT ON TABLE biz_like IS '点赞表';
COMMENT ON COLUMN biz_like.target_type IS '目标类型: 1-文章, 2-评论';

-- 收藏表
CREATE TABLE biz_favorite (
    id              BIGINT          PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    article_id      BIGINT          NOT NULL,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX uk_favorite ON biz_favorite(user_id, article_id);

COMMENT ON TABLE biz_favorite IS '收藏表';

-- ============================================================
-- 文件表
-- ============================================================

CREATE TABLE biz_file (
    id              BIGINT          PRIMARY KEY,
    file_name       VARCHAR(200)    NOT NULL,
    original_name   VARCHAR(200)    NOT NULL,
    file_path       VARCHAR(500)    NOT NULL,
    file_url        VARCHAR(500),
    file_size        BIGINT          NOT NULL DEFAULT 0,
    file_type       VARCHAR(50),
    mime_type       VARCHAR(100),
    storage_type    VARCHAR(20)     NOT NULL DEFAULT 'MINIO',
    create_by       BIGINT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       BIGINT,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0
);

COMMENT ON TABLE biz_file IS '文件表';

-- ============================================================
-- AI模块
-- ============================================================

CREATE TABLE ai_conversation (
    id              BIGINT          PRIMARY KEY,
    user_id         BIGINT          NOT NULL,
    title           VARCHAR(200),
    model           VARCHAR(50),
    status          INT             NOT NULL DEFAULT 1,
    create_by       BIGINT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       BIGINT,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0
);

CREATE INDEX idx_conversation_user ON ai_conversation(user_id);

COMMENT ON TABLE ai_conversation IS 'AI对话表';

CREATE TABLE ai_message (
    id              BIGINT          PRIMARY KEY,
    conversation_id BIGINT          NOT NULL,
    role            VARCHAR(20)     NOT NULL,
    content         TEXT            NOT NULL,
    token_count     INT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_message_conversation ON ai_message(conversation_id);

COMMENT ON TABLE ai_message IS 'AI消息表';
COMMENT ON COLUMN ai_message.role IS '角色: system, user, assistant';

CREATE TABLE ai_prompt (
    id              BIGINT          PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    description     VARCHAR(200),
    prompt_text     TEXT            NOT NULL,
    category        VARCHAR(50),
    status          INT             NOT NULL DEFAULT 1,
    create_by       BIGINT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       BIGINT,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0
);

COMMENT ON TABLE ai_prompt IS 'AI提示词模板表';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 超级管理员 (密码: admin123, BCrypt加密)
INSERT INTO sys_user (id, username, password, nickname, status) VALUES
(1, 'admin', '\$2a\$10\$XwAhNL39ZxUwSWeAeR0l7uy2poYNJ2/mc0rLMbpJQIXvtFUJ8Ycla', '超级管理员', 1);

-- 超级管理员角色
INSERT INTO sys_role (id, role_name, role_key, description, status) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '超级管理员，拥有所有权限', 1);

-- 用户角色关联
INSERT INTO sys_user_role (id, user_id, role_id) VALUES (1, 1, 1);

-- 默认分类
INSERT INTO biz_category (id, name, slug, description, sort) VALUES
(1, '技术', 'tech', '技术相关文章', 1),
(2, '生活', 'life', '生活随笔', 2),
(3, '随想', 'thoughts', '思考与感悟', 3);

-- 默认标签
INSERT INTO biz_tag (id, name, slug, color, sort) VALUES
(1, 'Java', 'java', '#b07219', 1),
(2, 'Spring', 'spring', '#6db33f', 2),
(3, 'Vue', 'vue', '#42b883', 3),
(4, 'PostgreSQL', 'postgresql', '#336791', 4),
(5, 'Docker', 'docker', '#2496ed', 5),
(6, 'Kubernetes', 'kubernetes', '#326ce5', 6),
(7, 'AI', 'ai', '#ff6b6b', 7),
(8, '前端', 'frontend', '#f7df1e', 8);

-- ============================================================
-- 知识库模块
-- ============================================================

-- 启用 pgvector 扩展
CREATE EXTENSION IF NOT EXISTS vector;

-- 知识库文档表
CREATE TABLE kb_document (
    id              BIGINT          PRIMARY KEY,
    title           VARCHAR(200)    NOT NULL,
    file_type       VARCHAR(20),
    file_url        VARCHAR(500),
    file_size       BIGINT,
    content         TEXT,
    chunk_count     INT             NOT NULL DEFAULT 0,
    status          INT             NOT NULL DEFAULT 0,
    category_id     BIGINT,
    author_id       BIGINT,
    create_by       BIGINT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       BIGINT,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0
);

CREATE INDEX idx_document_status ON kb_document(status);
CREATE INDEX idx_document_category ON kb_document(category_id);

COMMENT ON TABLE kb_document IS '知识库文档表';
COMMENT ON COLUMN kb_document.status IS '状态: 0-待处理, 1-处理中, 2-就绪, 3-失败';

-- 知识库文本分块表
CREATE TABLE kb_chunk (
    id              BIGINT          PRIMARY KEY,
    document_id     BIGINT          NOT NULL,
    chunk_index     INT             NOT NULL,
    content         TEXT            NOT NULL,
    token_count     INT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_chunk_document ON kb_chunk(document_id);

COMMENT ON TABLE kb_chunk IS '知识库文本分块表';

-- 知识库向量嵌入表
CREATE TABLE kb_embedding (
    id              BIGINT          PRIMARY KEY,
    chunk_id        BIGINT          NOT NULL,
    document_id     BIGINT          NOT NULL,
    embedding       vector(1024),
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_embedding_chunk ON kb_embedding(chunk_id);
CREATE INDEX idx_embedding_document ON kb_embedding(document_id);

COMMENT ON TABLE kb_embedding IS '知识库向量嵌入表';
COMMENT ON COLUMN kb_embedding.embedding IS '向量嵌入 (pgvector, 维度1024)';

-- ============================================================
-- 项目管理模块
-- ============================================================

-- 项目表
CREATE TABLE pm_project (
    id              BIGINT          PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    description     TEXT,
    status          SMALLINT        NOT NULL DEFAULT 0,
    priority        SMALLINT        NOT NULL DEFAULT 1,
    owner_id        BIGINT,
    start_date      DATE,
    end_date        DATE,
    create_by       BIGINT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       BIGINT,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0
);

CREATE INDEX idx_project_owner ON pm_project(owner_id);
CREATE INDEX idx_project_status ON pm_project(status);

COMMENT ON TABLE pm_project IS '项目表';
COMMENT ON COLUMN pm_project.name IS '项目名称';
COMMENT ON COLUMN pm_project.description IS '项目描述';
COMMENT ON COLUMN pm_project.status IS '状态: 0-规划中, 1-进行中, 2-已完成, 3-已归档';
COMMENT ON COLUMN pm_project.priority IS '优先级: 0-低, 1-中, 2-高, 3-紧急';
COMMENT ON COLUMN pm_project.owner_id IS '负责人ID';
COMMENT ON COLUMN pm_project.start_date IS '开始日期';
COMMENT ON COLUMN pm_project.end_date IS '结束日期';

-- 任务表
CREATE TABLE pm_task (
    id              BIGINT          PRIMARY KEY,
    project_id      BIGINT          NOT NULL,
    title           VARCHAR(200)    NOT NULL,
    description     TEXT,
    status          SMALLINT        NOT NULL DEFAULT 0,
    priority        SMALLINT        NOT NULL DEFAULT 1,
    assignee_id     BIGINT,
    reporter_id     BIGINT,
    due_date        DATE,
    create_by       BIGINT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       BIGINT,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0
);

CREATE INDEX idx_task_project ON pm_task(project_id);
CREATE INDEX idx_task_assignee ON pm_task(assignee_id);
CREATE INDEX idx_task_status ON pm_task(status);

COMMENT ON TABLE pm_task IS '任务表';
COMMENT ON COLUMN pm_task.project_id IS '所属项目ID';
COMMENT ON COLUMN pm_task.title IS '任务标题';
COMMENT ON COLUMN pm_task.description IS '任务描述';
COMMENT ON COLUMN pm_task.status IS '状态: 0-待办, 1-进行中, 2-已完成, 3-已关闭';
COMMENT ON COLUMN pm_task.priority IS '优先级: 0-低, 1-中, 2-高, 3-紧急';
COMMENT ON COLUMN pm_task.assignee_id IS '执行人ID';
COMMENT ON COLUMN pm_task.reporter_id IS '报告人ID';
COMMENT ON COLUMN pm_task.due_date IS '截止日期';

-- Bug表
CREATE TABLE pm_bug (
    id              BIGINT          PRIMARY KEY,
    project_id      BIGINT          NOT NULL,
    title           VARCHAR(200)    NOT NULL,
    description     TEXT,
    severity        SMALLINT        NOT NULL DEFAULT 1,
    status          SMALLINT        NOT NULL DEFAULT 0,
    assignee_id     BIGINT,
    reporter_id     BIGINT,
    due_date        DATE,
    create_by       BIGINT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       BIGINT,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0
);

CREATE INDEX idx_bug_project ON pm_bug(project_id);
CREATE INDEX idx_bug_assignee ON pm_bug(assignee_id);
CREATE INDEX idx_bug_status ON pm_bug(status);

COMMENT ON TABLE pm_bug IS 'Bug表';
COMMENT ON COLUMN pm_bug.project_id IS '所属项目ID';
COMMENT ON COLUMN pm_bug.title IS 'Bug标题';
COMMENT ON COLUMN pm_bug.description IS 'Bug描述';
COMMENT ON COLUMN pm_bug.severity IS '严重程度: 0-轻微, 1-一般, 2-严重, 3-致命';
COMMENT ON COLUMN pm_bug.status IS '状态: 0-待确认, 1-已确认, 2-修复中, 3-已修复, 4-已关闭';
COMMENT ON COLUMN pm_bug.assignee_id IS '处理人ID';
COMMENT ON COLUMN pm_bug.reporter_id IS '报告人ID';
COMMENT ON COLUMN pm_bug.due_date IS '截止日期';

-- ============================================================
-- MCP 平台表
-- ============================================================

-- MCP 服务表
CREATE TABLE IF NOT EXISTS mcp_server (
    id              BIGINT          PRIMARY KEY,
    name            VARCHAR(100)    NOT NULL,
    description     TEXT,
    transport_type  VARCHAR(20)     NOT NULL DEFAULT 'stdio',
    endpoint        VARCHAR(500),
    command         VARCHAR(500),
    args            TEXT,
    env_vars        TEXT,
    status          SMALLINT        NOT NULL DEFAULT 1,
    author_id       BIGINT,
    create_by       BIGINT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       BIGINT,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0
);

CREATE INDEX idx_mcp_server_status ON mcp_server(status);
CREATE UNIQUE INDEX uk_mcp_server_name ON mcp_server(name) WHERE deleted = 0;

COMMENT ON TABLE mcp_server IS 'MCP服务注册表';
COMMENT ON COLUMN mcp_server.name IS '服务名称';
COMMENT ON COLUMN mcp_server.description IS '服务描述';
COMMENT ON COLUMN mcp_server.transport_type IS '传输类型: stdio, sse, streamable_http';
COMMENT ON COLUMN mcp_server.endpoint IS 'SSE/HTTP端点地址';
COMMENT ON COLUMN mcp_server.command IS 'stdio启动命令';
COMMENT ON COLUMN mcp_server.args IS 'stdio启动参数(JSON数组)';
COMMENT ON COLUMN mcp_server.env_vars IS '环境变量(JSON对象)';
COMMENT ON COLUMN mcp_server.status IS '状态: 0-禁用, 1-启用';
COMMENT ON COLUMN mcp_server.author_id IS '创建者ID';

-- MCP 工具表
CREATE TABLE IF NOT EXISTS mcp_tool (
    id              BIGINT          PRIMARY KEY,
    server_id       BIGINT          NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    description     TEXT,
    input_schema    TEXT,
    output_schema   TEXT,
    status          SMALLINT        NOT NULL DEFAULT 1,
    create_by       BIGINT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       BIGINT,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0
);

CREATE INDEX idx_mcp_tool_server ON mcp_tool(server_id);
CREATE INDEX idx_mcp_tool_status ON mcp_tool(status);

COMMENT ON TABLE mcp_tool IS 'MCP工具表';
COMMENT ON COLUMN mcp_tool.server_id IS '关联MCP服务ID';
COMMENT ON COLUMN mcp_tool.name IS '工具名称';
COMMENT ON COLUMN mcp_tool.description IS '工具描述';
COMMENT ON COLUMN mcp_tool.input_schema IS '输入参数JSON Schema';
COMMENT ON COLUMN mcp_tool.output_schema IS '输出参数JSON Schema';
COMMENT ON COLUMN mcp_tool.status IS '状态: 0-禁用, 1-启用';

-- MCP 资源表
CREATE TABLE IF NOT EXISTS mcp_resource (
    id              BIGINT          PRIMARY KEY,
    server_id       BIGINT          NOT NULL,
    uri             VARCHAR(500)    NOT NULL,
    name            VARCHAR(100)    NOT NULL,
    description     TEXT,
    mime_type       VARCHAR(100),
    status          SMALLINT        NOT NULL DEFAULT 1,
    create_by       BIGINT,
    create_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_by       BIGINT,
    update_time     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted         INT             NOT NULL DEFAULT 0
);

CREATE INDEX idx_mcp_resource_server ON mcp_resource(server_id);
CREATE INDEX idx_mcp_resource_status ON mcp_resource(status);

COMMENT ON TABLE mcp_resource IS 'MCP资源表';
COMMENT ON COLUMN mcp_resource.server_id IS '关联MCP服务ID';
COMMENT ON COLUMN mcp_resource.uri IS '资源URI';
COMMENT ON COLUMN mcp_resource.name IS '资源名称';
COMMENT ON COLUMN mcp_resource.description IS '资源描述';
COMMENT ON COLUMN mcp_resource.mime_type IS 'MIME类型';
COMMENT ON COLUMN mcp_resource.status IS '状态: 0-禁用, 1-启用';
