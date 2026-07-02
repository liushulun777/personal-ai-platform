-- ============================================================
-- 报表平台示例数据
-- ============================================================

-- 清理旧数据（幂等）
DELETE FROM report_schedule;
DELETE FROM report_permission;
DELETE FROM report_log;
DELETE FROM report_definition;
DELETE FROM report_dashboard;
DELETE FROM report_dataset;
DELETE FROM report_datasource;

-- ============================================================
-- 1. 数据源示例数据
-- ============================================================
INSERT INTO report_datasource (id, name, type, config, status, remark, create_by, create_time, update_by, update_time, deleted) VALUES
(1, '本地PostgreSQL', 'POSTGRESQL', '{"host":"172.18.0.34","port":5432,"database":"platform","username":"postgres","password":"postgres"}', 1, '平台主数据库', 1, NOW(), 1, NOW(), 0),
(2, '示例MySQL', 'MYSQL', '{"host":"localhost","port":3306,"database":"demo","username":"root","password":"root"}', 1, '示例MySQL数据源', 1, NOW(), 1, NOW(), 0),
(3, '天气API', 'API', '{"url":"https://api.weather.com/v1/current","method":"GET","headers":{"Authorization":"Bearer xxx"},"responseMapping":"data"}', 1, '天气数据API', 1, NOW(), 1, NOW(), 0);

-- ============================================================
-- 2. 数据集示例数据
-- ============================================================
INSERT INTO report_dataset (id, name, type, source_id, config, fields, status, remark, create_by, create_time, update_by, update_time, deleted) VALUES
(1, '用户统计', 'SQL', 1, '{"sql":"SELECT DATE(create_time) as date, COUNT(*) as user_count FROM sys_user WHERE deleted = 0 GROUP BY DATE(create_time) ORDER BY date DESC LIMIT 30"}', '[{"name":"date","label":"日期","type":"DATE"},{"name":"user_count","label":"用户数","type":"NUMBER"}]', 1, '按日统计用户注册数', 1, NOW(), 1, NOW(), 0),
(2, '文章统计', 'SQL', 1, '{"sql":"SELECT DATE(create_time) as date, COUNT(*) as article_count, SUM(view_count) as total_views FROM biz_article WHERE deleted = 0 GROUP BY DATE(create_time) ORDER BY date DESC LIMIT 30"}', '[{"name":"date","label":"日期","type":"DATE"},{"name":"article_count","label":"文章数","type":"NUMBER"},{"name":"total_views","label":"总浏览量","type":"NUMBER"}]', 1, '按日统计文章发布', 1, NOW(), 1, NOW(), 0),
(3, '分类分布', 'SQL', 1, '{"sql":"SELECT c.name as category, COUNT(a.id) as count FROM biz_category c LEFT JOIN biz_article a ON c.id = a.category_id AND a.deleted = 0 WHERE c.deleted = 0 GROUP BY c.id, c.name ORDER BY count DESC"}', '[{"name":"category","label":"分类","type":"STRING"},{"name":"count","label":"文章数","type":"NUMBER"}]', 1, '文章分类分布', 1, NOW(), 1, NOW(), 0),
(4, '系统概览', 'SQL', 1, '{"sql":"SELECT (SELECT COUNT(*) FROM sys_user WHERE deleted = 0) as user_count, (SELECT COUNT(*) FROM biz_article WHERE deleted = 0) as article_count, (SELECT COUNT(*) FROM biz_category WHERE deleted = 0) as category_count, (SELECT COUNT(*) FROM biz_comment WHERE deleted = 0) as comment_count"}', '[{"name":"user_count","label":"用户数","type":"NUMBER"},{"name":"article_count","label":"文章数","type":"NUMBER"},{"name":"category_count","label":"分类数","type":"NUMBER"},{"name":"comment_count","label":"评论数","type":"NUMBER"}]', 1, '系统数据概览', 1, NOW(), 1, NOW(), 0),
(5, '热门文章', 'SQL', 1, '{"sql":"SELECT title, view_count, like_count, comment_count FROM biz_article WHERE deleted = 0 AND status = 1 ORDER BY view_count DESC LIMIT 10"}', '[{"name":"title","label":"标题","type":"STRING"},{"name":"view_count","label":"浏览量","type":"NUMBER"},{"name":"like_count","label":"点赞数","type":"NUMBER"},{"name":"comment_count","label":"评论数","type":"NUMBER"}]', 1, '热门文章TOP10', 1, NOW(), 1, NOW(), 0);

-- ============================================================
-- 3. 报表定义示例数据
-- ============================================================
INSERT INTO report_definition (id, name, code, type, category, config, status, version, remark, create_by, create_time, update_by, update_time, deleted) VALUES
(1, '用户增长报表', 'user_growth', 'NORMAL', '用户分析', '{"canvas":{"width":1200,"height":800,"background":"#ffffff"},"components":[{"id":1,"type":"text","x":20,"y":20,"width":300,"height":40,"config":{"content":"用户增长趋势","fontSize":24,"fontWeight":"bold"}},{"id":2,"type":"line","x":20,"y":80,"width":560,"height":350,"config":{"datasetId":"1","chartType":"line"}},{"id":3,"type":"text","x":600,"y":20,"width":300,"height":40,"config":{"content":"数据明细","fontSize":18}},{"id":4,"type":"table","x":600,"y":80,"width":580,"height":350,"config":{"datasetId":"1"}}],"params":[]}', 1, 1, '用户增长趋势分析报表', 1, NOW(), 1, NOW(), 0),
(2, '内容分析报表', 'content_analysis', 'NORMAL', '内容分析', '{"canvas":{"width":1200,"height":900,"background":"#ffffff"},"components":[{"id":1,"type":"text","x":20,"y":20,"width":400,"height":40,"config":{"content":"内容数据分析","fontSize":24,"fontWeight":"bold"}},{"id":2,"type":"bar","x":20,"y":80,"width":560,"height":300,"config":{"datasetId":"2","chartType":"bar"}},{"id":3,"type":"pie","x":600,"y":80,"width":560,"height":300,"config":{"datasetId":"3","chartType":"pie"}},{"id":4,"type":"table","x":20,"y":400,"width":1160,"height":250,"config":{"datasetId":"5"}}],"params":[]}', 1, 1, '文章内容分析报表', 1, NOW(), 1, NOW(), 0),
(3, '系统概览报表', 'system_overview', 'NORMAL', '系统监控', '{"canvas":{"width":1000,"height":600,"background":"#f5f7fa"},"components":[{"id":1,"type":"text","x":20,"y":20,"width":300,"height":40,"config":{"content":"系统数据概览","fontSize":24,"fontWeight":"bold"}},{"id":2,"type":"gauge","x":20,"y":80,"width":220,"height":200,"config":{"datasetId":"4","title":"用户数","valueField":"user_count"}},{"id":3,"type":"gauge","x":260,"y":80,"width":220,"height":200,"config":{"datasetId":"4","title":"文章数","valueField":"article_count"}},{"id":4,"type":"gauge","x":500,"y":80,"width":220,"height":200,"config":{"datasetId":"4","title":"分类数","valueField":"category_count"}},{"id":5,"type":"gauge","x":740,"y":80,"width":220,"height":200,"config":{"datasetId":"4","title":"评论数","valueField":"comment_count"}}],"params":[]}', 1, 1, '系统核心指标概览', 1, NOW(), 1, NOW(), 0);

-- ============================================================
-- 4. 大屏示例数据
-- ============================================================
INSERT INTO report_dashboard (id, name, config, status, share_url, remark, create_by, create_time, update_by, update_time, deleted) VALUES
(1, '数据监控大屏', '{"canvas":{"width":1920,"height":1080,"background":"#0a1628"},"components":[{"id":1,"type":"text","x":710,"y":30,"width":500,"height":60,"config":{"content":"数据监控中心","fontSize":36,"fontWeight":"bold","color":"#00d4ff","textAlign":"center"}},{"id":2,"type":"line","x":50,"y":120,"width":580,"height":400,"config":{"datasetId":"1","chartType":"line"}},{"id":3,"type":"bar","x":670,"y":120,"width":580,"height":400,"config":{"datasetId":"2","chartType":"bar"}},{"id":4,"type":"pie","x":1290,"y":120,"width":580,"height":400,"config":{"datasetId":"3","chartType":"pie"}},{"id":5,"type":"table","x":50,"y":550,"width":1820,"height":480,"config":{"datasetId":"5"}}]}', 1, NULL, '数据实时监控大屏', 1, NOW(), 1, NOW(), 0),
(2, '运营分析大屏', '{"canvas":{"width":1920,"height":1080,"background":"linear-gradient(135deg, #1a1a2e 0%, #16213e 100%)"},"components":[{"id":1,"type":"text","x":710,"y":30,"width":500,"height":60,"config":{"content":"运营数据分析","fontSize":36,"fontWeight":"bold","color":"#00ff88","textAlign":"center"}},{"id":2,"type":"gauge","x":100,"y":150,"width":350,"height":300,"config":{"datasetId":"4","title":"用户总数","valueField":"user_count"}},{"id":3,"type":"gauge","x":500,"y":150,"width":350,"height":300,"config":{"datasetId":"4","title":"文章总数","valueField":"article_count"}},{"id":4,"type":"gauge","x":900,"y":150,"width":350,"height":300,"config":{"datasetId":"4","title":"评论总数","valueField":"comment_count"}},{"id":5,"type":"line","x":100,"y":500,"width":800,"height":500,"config":{"datasetId":"2","chartType":"line"}},{"id":6,"type":"bar","x":1000,"y":500,"width":850,"height":500,"config":{"datasetId":"1","chartType":"bar"}}]}', 1, NULL, '运营数据可视化大屏', 1, NOW(), 1, NOW(), 0);

-- ============================================================
-- 5. 调度任务示例数据
-- ============================================================
INSERT INTO report_schedule (id, name, report_id, cron, config, status, last_execute_time, next_execute_time, remark, create_by, create_time, update_by, update_time, deleted) VALUES
(1, '每日用户报表', 1, '0 0 9 * * ?', '{"exportFormat":"excel","email":"admin@example.com"}', 1, NULL, NULL, '每天早上9点生成用户报表', 1, NOW(), 1, NOW(), 0),
(2, '每周内容报表', 2, '0 0 10 ? * MON', '{"exportFormat":"pdf","email":"admin@example.com"}', 0, NULL, NULL, '每周一上午10点生成内容报表', 1, NOW(), 1, NOW(), 0);

-- ============================================================
-- 重置序列
-- ============================================================
SELECT setval('report_datasource_id_seq', (SELECT MAX(id) FROM report_datasource));
SELECT setval('report_dataset_id_seq', (SELECT MAX(id) FROM report_dataset));
SELECT setval('report_definition_id_seq', (SELECT MAX(id) FROM report_definition));
SELECT setval('report_dashboard_id_seq', (SELECT MAX(id) FROM report_dashboard));
SELECT setval('report_schedule_id_seq', (SELECT MAX(id) FROM report_schedule));
