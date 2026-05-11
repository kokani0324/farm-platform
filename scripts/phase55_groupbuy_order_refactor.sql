-- ============================================================
-- Phase 5.5 — GroupBuy 整單重構遷移腳本
-- 目的：
--   1. 移除 orders.group_buy_id 欄位（含 FK 與 index）
--   2. 砍掉舊團購相關 4 表，讓 Hibernate 下次啟動重建（含新 group_buy_orders）
-- 注意：
--   * 這個腳本只動「團購相關」資料表；一般訂單 (orders) 與 order_items 保留。
--   * 執行前請確認資料庫名稱為 farm_platform。
--   * 跑完後啟動 backend，application.yml 的 spring.jpa.hibernate.ddl-auto=update
--     會自動建出新表結構（新欄位 / 新 enum / GroupBuyOrder）。
-- ============================================================

USE farm_platform;

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------
-- 1) 拔掉 orders 對 group_buys 的 FK + 欄位
--    FK 名稱由 MySQL 自動命名，需先動態查出後 drop。
-- ----------------------------------------------------------------
SET @fk_name := (
    SELECT CONSTRAINT_NAME
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'orders'
      AND COLUMN_NAME = 'group_buy_id'
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1
);

SET @sql := IF(@fk_name IS NOT NULL,
    CONCAT('ALTER TABLE orders DROP FOREIGN KEY ', @fk_name),
    'SELECT "no FK on orders.group_buy_id"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 拔掉 idx_order_groupbuy（若存在）
SET @idx_name := (
    SELECT INDEX_NAME
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'orders'
      AND INDEX_NAME = 'idx_order_groupbuy'
    LIMIT 1
);
SET @sql := IF(@idx_name IS NOT NULL,
    'ALTER TABLE orders DROP INDEX idx_order_groupbuy',
    'SELECT "no idx_order_groupbuy"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 拔掉 group_buy_id 欄位（若存在）
SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'orders'
      AND COLUMN_NAME = 'group_buy_id'
);
SET @sql := IF(@col_exists > 0,
    'ALTER TABLE orders DROP COLUMN group_buy_id',
    'SELECT "no group_buy_id column on orders"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ----------------------------------------------------------------
-- 2) 砍掉舊團購表，讓 Hibernate 重建新 schema
-- ----------------------------------------------------------------
DROP TABLE IF EXISTS group_buy_orders;          -- 若先前有手動建過
DROP TABLE IF EXISTS group_buy_participations;
DROP TABLE IF EXISTS group_buys;
DROP TABLE IF EXISTS group_buy_requests;

SET FOREIGN_KEY_CHECKS = 1;

SELECT 'phase 5.5 migration done — restart backend to let Hibernate create new tables.' AS status;
