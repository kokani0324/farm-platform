-- ============================================
-- 農鮮禾渡 小農平台 資料庫初始化
-- 執行: mysql -u root -p < 01_init.sql
-- 或在 MySQL Workbench / phpMyAdmin 貼上執行
-- ============================================

CREATE DATABASE IF NOT EXISTS farm_platform
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE farm_platform;

-- 注意：JPA 啟動時會根據 Entity 自動建立 users 資料表（spring.jpa.hibernate.ddl-auto=update）
-- 此 SQL 僅用於建立資料庫本身。
-- 如需手動建立 users 表，可使用以下語法：

-- CREATE TABLE IF NOT EXISTS users (
--     id          BIGINT AUTO_INCREMENT PRIMARY KEY,
--     email       VARCHAR(100) NOT NULL UNIQUE,
--     password    VARCHAR(255) NOT NULL,
--     name        VARCHAR(50)  NOT NULL,
--     phone       VARCHAR(20),
--     role        VARCHAR(20)  NOT NULL,
--     enabled     TINYINT(1)   NOT NULL DEFAULT 1,
--     created_at  DATETIME     NOT NULL,
--     updated_at  DATETIME     NOT NULL
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
