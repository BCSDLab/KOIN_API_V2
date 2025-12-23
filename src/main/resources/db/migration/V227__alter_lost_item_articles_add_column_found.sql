ALTER TABLE `koin`.`lost_item_articles`
    ADD COLUMN `is_found` tinyint(1) NOT NULL DEFAULT 0 COMMENT '분실물 찾음 여부' AFTER `is_deleted`,
    ADD COLUMN `found_at` TIMESTAMP NULL COMMENT '분실물 찾은 시각' AFTER `is_found`;
