ALTER TABLE `shops`
    ADD COLUMN `introduction` TEXT NULL COMMENT '가게 소개' AFTER `description`,
    ADD COLUMN `notice`       TEXT NULL COMMENT '가게 알림'  AFTER `introduction`;
