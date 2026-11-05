-- 慧医数字医疗应用系统 · 扩展功能「销售地点地图」数据库变更
--
-- 作用：给 sale 表补经纬度和地址三列，并给种子数据造出示例坐标（青岛附近）。
-- 幂等：用存储过程判断列是否存在，重复执行不会报错。
--
-- 用法：mysql -h127.0.0.1 -P3307 -uroot -p123456 --default-character-set=utf8mb4 bin_text < sql/sale_ext.sql

DROP PROCEDURE IF EXISTS add_sale_map_columns;

DELIMITER $$
CREATE PROCEDURE add_sale_map_columns()
BEGIN
    IF NOT EXISTS(SELECT 1 FROM information_schema.columns
                  WHERE table_schema = DATABASE() AND table_name = 'sale' AND column_name = 'lng') THEN
        ALTER TABLE sale ADD COLUMN lng DOUBLE NULL COMMENT '经度';
    END IF;
    IF NOT EXISTS(SELECT 1 FROM information_schema.columns
                  WHERE table_schema = DATABASE() AND table_name = 'sale' AND column_name = 'lat') THEN
        ALTER TABLE sale ADD COLUMN lat DOUBLE NULL COMMENT '纬度';
    END IF;
    IF NOT EXISTS(SELECT 1 FROM information_schema.columns
                  WHERE table_schema = DATABASE() AND table_name = 'sale' AND column_name = 'address') THEN
        ALTER TABLE sale ADD COLUMN address VARCHAR(255) NULL COMMENT '门店地址';
    END IF;
END$$
DELIMITER ;

CALL add_sale_map_columns();
DROP PROCEDURE IF EXISTS add_sale_map_columns;

-- 给还没有坐标的药店补示例点：以青岛市南区为中心散开，保证地图上有多个标记
UPDATE sale
SET lng = 120.382600 + (sale_id % 9) * 0.009,
    lat = 36.067100 + (sale_id % 7) * 0.007,
    address = CONCAT('青岛市示例路 ', sale_id, ' 号')
WHERE lng IS NULL;

SELECT sale_id, sale_name, lng, lat, address FROM sale ORDER BY sale_id LIMIT 5;
