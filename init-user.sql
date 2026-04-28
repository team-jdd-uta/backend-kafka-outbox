-- init-user.sql (MariaDB)

-- --------------------------
-- DB / 사용자
-- --------------------------
CREATE DATABASE IF NOT EXISTS app_target;

CREATE USER IF NOT EXISTS 'app_target'@'%' IDENTIFIED BY 'app_target1234';
GRANT ALL PRIVILEGES ON app_target.* TO 'app_target'@'%';
FLUSH PRIVILEGES;

USE app_target;

-- --------------------------
-- 기존 테이블 정리
-- --------------------------
DROP TABLE IF EXISTS watch_history;
DROP TABLE IF EXISTS follows;
DROP TABLE IF EXISTS video;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS customer;

-- --------------------------
-- Customer Table
-- --------------------------
CREATE TABLE customer (
    user_id VARCHAR(50) PRIMARY KEY,
    `password` VARCHAR(255),
    name VARCHAR(100),
    created_at TIMESTAMP NULL
) ENGINE=InnoDB;

-- --------------------------
-- Category Table
-- --------------------------
CREATE TABLE category (
    category_id BIGINT PRIMARY KEY,
    category_name VARCHAR(100),
    parent_category_id BIGINT,
    CONSTRAINT fk_category_parent
        FOREIGN KEY (parent_category_id)
        REFERENCES category(category_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- --------------------------
-- Video Table
-- --------------------------
CREATE TABLE video (
    video_id BIGINT PRIMARY KEY,
    video_name VARCHAR(255),
    started_at TIMESTAMP NULL,
    ended_at TIMESTAMP NULL,
    category_id BIGINT,
    CONSTRAINT fk_video_category
        FOREIGN KEY (category_id)
        REFERENCES category(category_id)
) ENGINE=InnoDB;

-- --------------------------
-- Follows Table
-- --------------------------
CREATE TABLE follows (
    following_user_id VARCHAR(50),
    followed_user_id VARCHAR(50),
    followed_at TIMESTAMP NULL,
    CONSTRAINT pk_follows PRIMARY KEY (following_user_id, followed_user_id),
    CONSTRAINT fk_follows_following FOREIGN KEY (following_user_id)
        REFERENCES customer(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_follows_followed FOREIGN KEY (followed_user_id)
        REFERENCES customer(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_follows_following_user_id ON follows(following_user_id, followed_user_id);
CREATE INDEX idx_follows_followed_user_id ON follows(followed_user_id, following_user_id);

-- --------------------------
-- Watch History Table (No Partition)
-- --------------------------
CREATE TABLE watch_history (
    user_id VARCHAR(50),
    video_id BIGINT,
    started_at TIMESTAMP NULL,
    ended_at TIMESTAMP NULL,
    CONSTRAINT fk_watch_user FOREIGN KEY (user_id)
        REFERENCES customer(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_watch_video FOREIGN KEY (video_id)
        REFERENCES video(video_id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_watch_user_id ON watch_history(user_id, started_at);
CREATE INDEX idx_watch_video_id ON watch_history(video_id, started_at);

-- --------------------------
-- 1. Category (10개)
-- --------------------------
INSERT INTO category (category_id, category_name, parent_category_id)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 10
)
SELECT n, CONCAT('Category_', n), NULL
FROM seq;

-- --------------------------
-- 2. Customer (100개)
-- --------------------------
INSERT INTO customer (user_id, `password`, name, created_at)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 100
)
SELECT
    CONCAT('user', LPAD(n, 3, '0')),
    CONCAT('pass', n),
    CONCAT('User ', n),
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY)
FROM seq;

-- --------------------------
-- 3. Video (100개)
-- --------------------------
INSERT INTO video (video_id, video_name, started_at, ended_at, category_id)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 100
)
SELECT
    n,
    CONCAT('Video_', n),
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY),
    DATE_ADD(NOW(), INTERVAL (FLOOR(RAND() * 10) + 1) DAY),
    ((n - 1) % 10) + 1
FROM seq;

-- --------------------------
-- 4. Follows (100개)
-- --------------------------
INSERT INTO follows (following_user_id, followed_user_id, followed_at)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 100
)
SELECT
    CONCAT('user', LPAD(n, 3, '0')),
    CONCAT('user', LPAD(((n + FLOOR(RAND() * 99)) % 100) + 1, 3, '0')),
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 365) DAY)
FROM seq;

-- --------------------------
-- 5. Watch History (100개)
-- --------------------------
INSERT INTO watch_history (user_id, video_id, started_at, ended_at)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 100
)
SELECT
    CONCAT('user', LPAD(((n - 1) % 100) + 1, 3, '0')),
    ((n - 1) % 100) + 1,
    DATE_SUB(NOW(), INTERVAL FLOOR(RAND() * 30) DAY),
    DATE_ADD(NOW(), INTERVAL FLOOR(RAND() * 10) DAY)
FROM seq;
