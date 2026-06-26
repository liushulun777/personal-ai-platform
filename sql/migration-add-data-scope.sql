-- 补建 sys_role.data_scope 字段
-- 如果已存在则跳过

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'sys_role' AND column_name = 'data_scope'
    ) THEN
        ALTER TABLE sys_role ADD COLUMN data_scope INT NOT NULL DEFAULT 1;
        COMMENT ON COLUMN sys_role.data_scope IS '数据范围: 1-全部数据, 2-仅本人数据';

        -- 作者角色设为仅本人数据
        UPDATE sys_role SET data_scope = 2 WHERE role_key = 'AUTHOR';
    END IF;
END $$;
