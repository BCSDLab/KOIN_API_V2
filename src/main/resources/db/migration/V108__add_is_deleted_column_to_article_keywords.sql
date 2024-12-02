ALTER TABLE `article_keywords`
    ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 AFTER `updated_at`;

ALTER TABLE `article_keyword_user_map`
    ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 AFTER `updated_at`;
