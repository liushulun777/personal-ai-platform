-- 为 pm_task 表添加排序和预估工时字段
ALTER TABLE pm_task ADD COLUMN IF NOT EXISTS sort_order INT DEFAULT 0;
ALTER TABLE pm_task ADD COLUMN IF NOT EXISTS estimated_hours INT DEFAULT 0;

-- 添加注释
COMMENT ON COLUMN pm_task.sort_order IS '排序顺序（用于任务执行顺序）';
COMMENT ON COLUMN pm_task.estimated_hours IS '预估工时（小时）';

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_pm_task_sort_order ON pm_task(sort_order);
