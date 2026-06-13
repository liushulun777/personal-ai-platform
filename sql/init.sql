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
