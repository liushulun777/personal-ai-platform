-- ============================================================
-- 菜单 + 角色 + 权限 初始化脚本
-- 基于 platform-web 路由结构生成
-- ============================================================

-- 清理旧数据（幂等）
DELETE FROM sys_role_menu WHERE role_id IN (1, 2);
DELETE FROM sys_user_role WHERE role_id = 2;
DELETE FROM sys_menu WHERE id BETWEEN 100 AND 199;
DELETE FROM sys_role WHERE id = 2;

-- ============================================================
-- 菜单数据 (id: 100-199)
-- ============================================================

-- 一级目录
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort, visible, status) VALUES
(100, 0, '仪表盘', '/dashboard', NULL, 'dashboard', 0, NULL, 1, 1, 1),
(101, 0, '系统管理', '/system', NULL, 'settings', 0, NULL, 10, 1, 1),
(102, 0, '博客管理', '/blog', NULL, 'document-text', 0, NULL, 20, 1, 1),
(103, 0, '知识库', '/knowledge', NULL, 'library', 0, NULL, 30, 1, 1),
(104, 0, 'AI 助手', '/ai', NULL, 'sparkles', 0, NULL, 40, 1, 1),
(105, 0, '项目管理', '/project', NULL, 'folder-open', 0, NULL, 50, 1, 1),
(106, 0, 'MCP 平台', '/mcp', NULL, 'cube', 0, NULL, 60, 1, 1),
(107, 0, 'Agent', '/agent', NULL, 'robot', 0, NULL, 70, 1, 1);

-- 仪表盘
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort, visible, status) VALUES
(110, 100, '仪表盘', 'dashboard', 'modules/system/views/DashboardView.vue', NULL, 1, 'system:dashboard:view', 1, 1, 1);

-- 系统管理 - 子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort, visible, status) VALUES
(111, 101, '用户管理', 'users', 'modules/system/views/UserListView.vue', NULL, 1, 'system:user:list', 1, 1, 1),
(112, 101, '角色管理', 'roles', 'modules/system/views/RoleListView.vue', NULL, 1, 'system:role:list', 2, 1, 1),
(113, 101, '菜单管理', 'menus', 'modules/system/views/MenuListView.vue', NULL, 1, 'system:menu:list', 3, 1, 1),
(114, 101, '字典管理', 'dicts', 'modules/system/views/DictListView.vue', NULL, 1, 'system:dict:list', 4, 1, 1),
(115, 101, '操作日志', 'logs', 'modules/system/views/LogListView.vue', NULL, 1, 'system:log:list', 5, 1, 1);

-- 系统管理 - 按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort, visible, status) VALUES
(116, 111, '用户新增', NULL, NULL, NULL, 2, 'system:user:add', 1, 0, 1),
(117, 111, '用户编辑', NULL, NULL, NULL, 2, 'system:user:edit', 2, 0, 1),
(118, 111, '用户删除', NULL, NULL, NULL, 2, 'system:user:delete', 3, 0, 1),
(119, 112, '角色新增', NULL, NULL, NULL, 2, 'system:role:add', 1, 0, 1),
(120, 112, '角色编辑', NULL, NULL, NULL, 2, 'system:role:edit', 2, 0, 1),
(121, 112, '角色删除', NULL, NULL, NULL, 2, 'system:role:delete', 3, 0, 1),
(122, 113, '菜单新增', NULL, NULL, NULL, 2, 'system:menu:add', 1, 0, 1),
(123, 113, '菜单编辑', NULL, NULL, NULL, 2, 'system:menu:edit', 2, 0, 1),
(124, 113, '菜单删除', NULL, NULL, NULL, 2, 'system:menu:delete', 3, 0, 1);

-- 博客管理 - 子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort, visible, status) VALUES
(130, 102, '文章管理', 'articles', 'modules/blog/views/ArticleListView.vue', NULL, 1, 'blog:article:list', 1, 1, 1),
(131, 102, '分类管理', 'categories', 'modules/blog/views/CategoryListView.vue', NULL, 1, 'blog:category:list', 2, 1, 1),
(132, 102, '标签管理', 'tags', 'modules/blog/views/TagListView.vue', NULL, 1, 'blog:tag:list', 3, 1, 1);

-- 博客管理 - 按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort, visible, status) VALUES
(133, 130, '文章新增', NULL, NULL, NULL, 2, 'blog:article:add', 1, 0, 1),
(134, 130, '文章编辑', NULL, NULL, NULL, 2, 'blog:article:edit', 2, 0, 1),
(135, 130, '文章删除', NULL, NULL, NULL, 2, 'blog:article:delete', 3, 0, 1),
(136, 130, '文章发布', NULL, NULL, NULL, 2, 'blog:article:publish', 4, 0, 1),
(137, 131, '分类新增', NULL, NULL, NULL, 2, 'blog:category:add', 1, 0, 1),
(138, 131, '分类编辑', NULL, NULL, NULL, 2, 'blog:category:edit', 2, 0, 1),
(139, 131, '分类删除', NULL, NULL, NULL, 2, 'blog:category:delete', 3, 0, 1),
(140, 132, '标签新增', NULL, NULL, NULL, 2, 'blog:tag:add', 1, 0, 1),
(141, 132, '标签编辑', NULL, NULL, NULL, 2, 'blog:tag:edit', 2, 0, 1),
(142, 132, '标签删除', NULL, NULL, NULL, 2, 'blog:tag:delete', 3, 0, 1);

-- 知识库 - 子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort, visible, status) VALUES
(143, 103, '文档管理', 'documents', 'modules/knowledge/views/DocumentListView.vue', NULL, 1, 'knowledge:document:list', 1, 1, 1),
(144, 103, '知识问答', 'chat', 'modules/knowledge/views/KnowledgeChatView.vue', NULL, 1, 'knowledge:chat:use', 2, 1, 1);

-- 知识库 - 按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort, visible, status) VALUES
(148, 143, '文档上传', NULL, NULL, NULL, 2, 'knowledge:document:upload', 1, 0, 1),
(149, 143, '文档删除', NULL, NULL, NULL, 2, 'knowledge:document:delete', 2, 0, 1),
(165, 143, '文档重新处理', NULL, NULL, NULL, 2, 'knowledge:document:reprocess', 3, 0, 1);

-- AI 助手 - 子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort, visible, status) VALUES
(145, 104, 'AI 对话', 'chat', 'modules/ai/views/AiChatView.vue', NULL, 1, 'ai:chat:use', 1, 1, 1),
(146, 104, 'Prompt 模板', 'prompts', 'modules/ai/views/PromptListView.vue', NULL, 1, 'ai:prompt:list', 2, 1, 1),
(147, 104, 'Prompt 市场', 'market', 'modules/ai/views/PromptMarketView.vue', NULL, 1, 'ai:prompt:market', 3, 1, 1);

-- AI 助手 - 按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort, visible, status) VALUES
(166, 146, 'Prompt新增', NULL, NULL, NULL, 2, 'ai:prompt:add', 1, 0, 1),
(167, 146, 'Prompt编辑', NULL, NULL, NULL, 2, 'ai:prompt:edit', 2, 0, 1),
(168, 146, 'Prompt删除', NULL, NULL, NULL, 2, 'ai:prompt:delete', 3, 0, 1);

-- 项目管理 - 子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort, visible, status) VALUES
(150, 105, '项目管理', 'projects', 'modules/project/views/ProjectListView.vue', NULL, 1, 'project:project:list', 1, 1, 1),
(151, 105, '任务管理', 'tasks', 'modules/project/views/TaskListView.vue', NULL, 1, 'project:task:list', 2, 1, 1),
(152, 105, '任务看板', 'tasks/board', 'modules/project/views/TaskBoardView.vue', NULL, 1, 'project:task:board', 3, 1, 1),
(153, 105, 'Bug管理', 'bugs', 'modules/project/views/BugListView.vue', NULL, 1, 'project:bug:list', 4, 1, 1),
(154, 105, 'Bug统计', 'bugs/stats', 'modules/project/views/BugStatsView.vue', NULL, 1, 'project:bug:stats', 5, 1, 1),
(155, 105, '需求讨论', 'requirement', 'modules/project/views/RequirementDiscussionView.vue', NULL, 1, 'project:requirement:list', 6, 1, 1);

-- 项目管理 - 按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort, visible, status) VALUES
(156, 150, '项目新增', NULL, NULL, NULL, 2, 'project:project:add', 1, 0, 1),
(157, 150, '项目编辑', NULL, NULL, NULL, 2, 'project:project:edit', 2, 0, 1),
(158, 150, '项目删除', NULL, NULL, NULL, 2, 'project:project:delete', 3, 0, 1),
(159, 151, '任务新增', NULL, NULL, NULL, 2, 'project:task:add', 1, 0, 1),
(160, 151, '任务编辑', NULL, NULL, NULL, 2, 'project:task:edit', 2, 0, 1),
(161, 151, '任务删除', NULL, NULL, NULL, 2, 'project:task:delete', 3, 0, 1),
(162, 153, 'Bug新增', NULL, NULL, NULL, 2, 'project:bug:add', 1, 0, 1),
(163, 153, 'Bug编辑', NULL, NULL, NULL, 2, 'project:bug:edit', 2, 0, 1),
(164, 153, 'Bug删除', NULL, NULL, NULL, 2, 'project:bug:delete', 3, 0, 1);

-- MCP 平台 - 子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort, visible, status) VALUES
(170, 106, 'MCP 服务', 'servers', 'modules/mcp/views/McpServerListView.vue', NULL, 1, 'mcp:server:list', 1, 1, 1),
(171, 106, 'MCP 工具', 'tools', 'modules/mcp/views/McpToolListView.vue', NULL, 1, 'mcp:tool:list', 2, 1, 1);

-- MCP 平台 - 按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort, visible, status) VALUES
(172, 170, 'MCP服务新增', NULL, NULL, NULL, 2, 'mcp:server:add', 1, 0, 1),
(173, 170, 'MCP服务编辑', NULL, NULL, NULL, 2, 'mcp:server:edit', 2, 0, 1),
(174, 170, 'MCP服务删除', NULL, NULL, NULL, 2, 'mcp:server:delete', 3, 0, 1),
(175, 171, 'MCP工具新增', NULL, NULL, NULL, 2, 'mcp:tool:add', 1, 0, 1),
(176, 171, 'MCP工具删除', NULL, NULL, NULL, 2, 'mcp:tool:delete', 2, 0, 1),
(177, 171, 'MCP工具调用', NULL, NULL, NULL, 2, 'mcp:tool:invoke', 3, 0, 1),
(178, 171, 'MCP工具编辑', NULL, NULL, NULL, 2, 'mcp:tool:edit', 4, 0, 1);

-- Agent
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort, visible, status) VALUES
(180, 107, '执行记录', 'executions', 'modules/agent/views/AgentExecutionView.vue', NULL, 1, 'agent:execution:list', 1, 1, 1);

-- ============================================================
-- 角色数据
-- ============================================================

-- 普通角色：作者（可以发布管理自己的文章，数据范围：仅本人）
INSERT INTO sys_role (id, role_name, role_key, description, sort, status, data_scope) VALUES
(2, '作者', 'AUTHOR', '普通作者角色，可发布和管理自己的文章，使用AI助手和知识库', 1, 1, 2);

-- ============================================================
-- 角色-菜单关联
-- ============================================================

-- 超级管理员：拥有所有菜单权限
INSERT INTO sys_role_menu (id, role_id, menu_id)
SELECT 1000 + id, 1, id FROM sys_menu WHERE id BETWEEN 100 AND 199;

-- 作者角色：仪表盘 + 博客管理（含增删改查按钮）+ 知识库 + AI 助手
INSERT INTO sys_role_menu (id, role_id, menu_id) VALUES
-- 仪表盘
(2000, 2, 100),
(2001, 2, 110),
-- 博客管理
(2002, 2, 102),
(2003, 2, 130), (2004, 2, 131), (2005, 2, 132),
-- 博客按钮权限
(2006, 2, 133), (2007, 2, 134), (2008, 2, 135), (2009, 2, 136),
(2010, 2, 137), (2011, 2, 138), (2012, 2, 139),
(2013, 2, 140), (2014, 2, 141), (2015, 2, 142),
-- 知识库
(2016, 2, 103), (2017, 2, 143), (2018, 2, 144), (2023, 2, 148), (2024, 2, 149), (2025, 2, 165),
-- AI 助手
(2019, 2, 104), (2020, 2, 145), (2021, 2, 146), (2022, 2, 147),
-- AI Prompt按钮权限
(2026, 2, 166), (2027, 2, 167), (2028, 2, 168);

-- admin 用户关联作者角色（可选，admin 已有超级管理员角色）
-- INSERT INTO sys_user_role (id, user_id, role_id) VALUES (2, 1, 2);
