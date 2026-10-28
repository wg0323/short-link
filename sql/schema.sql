-- ============================================================
-- 短链系统建表脚本（MySQL 8.0）
-- 字符集统一 utf8mb4，引擎 InnoDB
-- ============================================================

-- 脚本里的表/字段注释是 UTF-8，客户端默认字符集不是 utf8mb4 时
-- 中文注释会以 latin1 解读后再存成 UTF-8，读出来就是乱码。这里强制指定。
SET NAMES utf8mb4;

CREATE DATABASE IF NOT EXISTS `short_link`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE `short_link`;

-- ------------------------------------------------------------
-- 用户
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
    `phone`         VARCHAR(20)     NOT NULL COMMENT '注册手机号',
    `password_hash` VARCHAR(100)    NOT NULL COMMENT 'BCrypt 密码摘要',
    `nickname`      VARCHAR(50)     NOT NULL DEFAULT '' COMMENT '昵称',
    `avatar`        VARCHAR(255)    NOT NULL DEFAULT '' COMMENT '头像对象名，如 avatar/1-xxxx.png，文件存 MinIO',
    `status`        TINYINT         NOT NULL DEFAULT 1 COMMENT '1 正常 0 禁用',
    `last_login_at` DATETIME                 DEFAULT NULL COMMENT '最近登录时间',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='注册用户';

-- ------------------------------------------------------------
-- 短链域名（后台维护，用户在生成时选择）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `domain`;
CREATE TABLE `domain`
(
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `domain`     VARCHAR(100)    NOT NULL COMMENT '短链域名，如 s.example.com',
    `is_default` TINYINT         NOT NULL DEFAULT 0 COMMENT '1 默认域名',
    `status`     TINYINT         NOT NULL DEFAULT 1 COMMENT '1 启用 0 停用',
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_domain` (`domain`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='短链域名';

-- ------------------------------------------------------------
-- 短链
-- url_hash 在自定义短码时写入 NULL：MySQL 唯一索引允许多个 NULL，
-- 因此幂等约束只作用于「系统生成的短码」。
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `short_link`;
CREATE TABLE `short_link`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `code`          VARCHAR(16)     NOT NULL COMMENT '短码，全局唯一',
    `domain_id`     BIGINT UNSIGNED NOT NULL COMMENT '所属短链域名',
    `user_id`       BIGINT UNSIGNED NOT NULL COMMENT '创建者',
    `origin_url`    VARCHAR(2048)   NOT NULL COMMENT '原始长链接',
    `url_hash`      CHAR(64)                 DEFAULT NULL COMMENT '原始链接 SHA-256，用于幂等',
    `remark`        VARCHAR(100)    NOT NULL DEFAULT '' COMMENT '备注名',
    `password_hash` VARCHAR(100)             DEFAULT NULL COMMENT '访问密码 BCrypt 摘要',
    `start_at`      DATETIME                 DEFAULT NULL COMMENT '生效开始时间',
    `end_at`        DATETIME                 DEFAULT NULL COMMENT '生效结束时间',
    `expire_at`     DATETIME                 DEFAULT NULL COMMENT '到期时间',
    `status`        TINYINT         NOT NULL DEFAULT 1 COMMENT '1 正常 0 禁用 2 已删除',
    `click_count`   BIGINT          NOT NULL DEFAULT 0 COMMENT '累计点击（冗余字段）',
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    UNIQUE KEY `uk_user_domain_hash` (`user_id`, `domain_id`, `url_hash`),
    KEY `idx_user_created` (`user_id`, `created_at`),
    KEY `idx_status_code` (`status`, `code`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='短链';

-- ------------------------------------------------------------
-- 短链按天汇总
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `link_stat_daily`;
CREATE TABLE `link_stat_daily`
(
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `code`       VARCHAR(16)     NOT NULL,
    `stat_date`  DATE            NOT NULL,
    `pv`         BIGINT          NOT NULL DEFAULT 0,
    `uv`         BIGINT          NOT NULL DEFAULT 0,
    `ip_count`   BIGINT          NOT NULL DEFAULT 0,
    `created_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code_date` (`code`, `stat_date`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='短链按天统计';

-- ------------------------------------------------------------
-- 短链按天 + 地域汇总
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `link_stat_region_daily`;
CREATE TABLE `link_stat_region_daily`
(
    `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `code`       VARCHAR(16)     NOT NULL,
    `stat_date`  DATE            NOT NULL,
    `province`   VARCHAR(32)     NOT NULL DEFAULT '',
    `city`       VARCHAR(64)     NOT NULL DEFAULT '',
    `pv`         BIGINT          NOT NULL DEFAULT 0,
    `uv`         BIGINT          NOT NULL DEFAULT 0,
    `ip_count`   BIGINT          NOT NULL DEFAULT 0,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code_date_region` (`code`, `stat_date`, `province`, `city`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='短链按天地域统计';

-- ------------------------------------------------------------
-- 访问明细：按天分区，滚动保留 90 天
-- 分区列必须包含在主键内，故主键为 (id, click_at)
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `click_log`;
CREATE TABLE `click_log`
(
    `id`       BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `code`     VARCHAR(16)     NOT NULL,
    `click_at` DATETIME        NOT NULL,
    `ip`       VARCHAR(45)     NOT NULL DEFAULT '',
    `province` VARCHAR(32)     NOT NULL DEFAULT '',
    `city`     VARCHAR(64)     NOT NULL DEFAULT '',
    `device`   VARCHAR(64)     NOT NULL DEFAULT '',
    `referer`  VARCHAR(255)    NOT NULL DEFAULT '',
    `ua`       VARCHAR(512)    NOT NULL DEFAULT '',
    PRIMARY KEY (`id`, `click_at`),
    KEY `idx_code_time` (`code`, `click_at`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='访问明细'
    PARTITION BY RANGE (TO_DAYS(`click_at`)) (
        PARTITION p202608 VALUES LESS THAN (TO_DAYS('2026-09-01')),
        PARTITION p202609 VALUES LESS THAN (TO_DAYS('2026-10-01')),
        PARTITION p202610 VALUES LESS THAN (TO_DAYS('2026-11-01')),
        PARTITION p202611 VALUES LESS THAN (TO_DAYS('2026-12-01')),
        PARTITION p202612 VALUES LESS THAN (TO_DAYS('2027-01-01')),
        PARTITION pmax VALUES LESS THAN MAXVALUE
        );

-- ------------------------------------------------------------
-- 后台管理员
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `admin_user`;
CREATE TABLE `admin_user`
(
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `username`      VARCHAR(50)     NOT NULL,
    `password_hash` VARCHAR(100)    NOT NULL,
    `nickname`      VARCHAR(50)     NOT NULL DEFAULT '',
    `status`        TINYINT         NOT NULL DEFAULT 1 COMMENT '1 正常 0 禁用',
    `last_login_at` DATETIME                 DEFAULT NULL,
    `created_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='后台管理员';

-- ------------------------------------------------------------
-- 号段表：短码发号器
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `segment`;
CREATE TABLE `segment`
(
    `biz_tag`    VARCHAR(50) NOT NULL,
    `max_id`     BIGINT      NOT NULL DEFAULT 0 COMMENT '当前已分配到的最大值',
    `step`       INT         NOT NULL DEFAULT 1000 COMMENT '每次预取步长',
    `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`biz_tag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='发号器号段';

-- ------------------------------------------------------------
-- 初始化数据
-- 管理员账号不在此处插入：密码需要 BCrypt 摘要，
-- 由 admin-api 首次启动时自动创建（admin / Admin@123456）
-- ------------------------------------------------------------
INSERT INTO `domain` (`domain`, `is_default`, `status`)
VALUES ('localhost:8081', 1, 1),
       ('go.localhost:8081', 0, 1);

INSERT INTO `segment` (`biz_tag`, `max_id`, `step`)
VALUES ('short_link', 100000, 1000);
