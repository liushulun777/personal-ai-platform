-- ============================================================
-- 报表平台菜单初始化脚本
-- ============================================================

-- 清理旧数据（幂等）
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 200 AND 299;
DELETE FROM sys_menu WHERE id BETWEEN 200 AND 299;

-- ============================================================
-- 菜单数据 (id: 200-299)
-- ============================================================

-- 一级目录：报表平台
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort, visible, status) VALUES
(200, 0, '报表平台', '/report', NULL, 'bar-chart', 0, NULL, 80, 1, 1);

-- 报表平台 - 子菜单
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort, visible, status) VALUES
(201, 200, '数据源管理', 'datasource', 'views/datasource/index.vue', NULL, 1, 'report:datasource:list', 1, 1, 1),
(202, 200, '数据集管理', 'dataset', 'views/dataset/index.vue', NULL, 1, 'report:dataset:list', 2, 1, 1),
(203, 200, '报表管理', 'report/list', 'views/report/list/index.vue', NULL, 1, 'report:report:list', 3, 1, 1),
(204, 200, '大屏管理', 'dashboard/list', 'views/dashboard/list/index.vue', NULL, 1, 'report:dashboard:list', 4, 1, 1),
(205, 200, '调度管理', 'schedule', 'views/schedule/index.vue', NULL, 1, 'report:schedule:list', 5, 1, 1);

-- 数据源管理 - 按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort, visible, status) VALUES
(211, 201, '数据源新增', NULL, NULL, NULL, 2, 'report:datasource:add', 1, 0, 1),
(212, 201, '数据源编辑', NULL, NULL, NULL, 2, 'report:datasource:edit', 2, 0, 1),
(213, 201, '数据源删除', NULL, NULL, NULL, 2, 'report:datasource:delete', 3, 0, 1),
(214, 201, '数据源测试', NULL, NULL, NULL, 2, 'report:datasource:test', 4, 0, 1);

-- 数据集管理 - 按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort, visible, status) VALUES
(221, 202, '数据集新增', NULL, NULL, NULL, 2, 'report:dataset:add', 1, 0, 1),
(222, 202, '数据集编辑', NULL, NULL, NULL, 2, 'report:dataset:edit', 2, 0, 1),
(223, 202, '数据集删除', NULL, NULL, NULL, 2, 'report:dataset:delete', 3, 0, 1),
(224, 202, '数据集预览', NULL, NULL, NULL, 2, 'report:dataset:preview', 4, 0, 1);

-- 报表管理 - 按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort, visible, status) VALUES
(231, 203, '报表新增', NULL, NULL, NULL, 2, 'report:report:add', 1, 0, 1),
(232, 203, '报表编辑', NULL, NULL, NULL, 2, 'report:report:edit', 2, 0, 1),
(233, 203, '报表删除', NULL, NULL, NULL, 2, 'report:report:delete', 3, 0, 1),
(234, 203, '报表设计', NULL, NULL, NULL, 2, 'report:report:design', 4, 0, 1),
(235, 203, '报表预览', NULL, NULL, NULL, 2, 'report:report:preview', 5, 0, 1),
(236, 203, '报表发布', NULL, NULL, NULL, 2, 'report:report:publish', 6, 0, 1),
(237, 203, '报表导出', NULL, NULL, NULL, 2, 'report:report:export', 7, 0, 1);

-- 大屏管理 - 按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort, visible, status) VALUES
(241, 204, '大屏新增', NULL, NULL, NULL, 2, 'report:dashboard:add', 1, 0, 1),
(242, 204, '大屏编辑', NULL, NULL, NULL, 2, 'report:dashboard:edit', 2, 0, 1),
(243, 204, '大屏删除', NULL, NULL, NULL, 2, 'report:dashboard:delete', 3, 0, 1),
(244, 204, '大屏设计', NULL, NULL, NULL, 2, 'report:dashboard:design', 4, 0, 1),
(245, 204, '大屏预览', NULL, NULL, NULL, 2, 'report:dashboard:preview', 5, 0, 1),
(246, 204, '大屏分享', NULL, NULL, NULL, 2, 'report:dashboard:share', 6, 0, 1);

-- 调度管理 - 按钮权限
INSERT INTO sys_menu (id, parent_id, menu_name, path, component, icon, menu_type, permission, sort, visible, status) VALUES
(251, 205, '调度新增', NULL, NULL, NULL, 2, 'report:schedule:add', 1, 0, 1),
(252, 205, '调度编辑', NULL, NULL, NULL, 2, 'report:schedule:edit', 2, 0, 1),
(253, 205, '调度删除', NULL, NULL, NULL, 2, 'report:schedule:delete', 3, 0, 1),
(254, 205, '调度启动', NULL, NULL, NULL, 2, 'report:schedule:start', 4, 0, 1),
(255, 205, '调度停止', NULL, NULL, NULL, 2, 'report:schedule:stop', 5, 0, 1);

-- ============================================================
-- 角色权限关联（管理员角色 id=1 拥有所有报表权限）
-- ============================================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, id FROM sys_menu WHERE id BETWEEN 200 AND 299;
