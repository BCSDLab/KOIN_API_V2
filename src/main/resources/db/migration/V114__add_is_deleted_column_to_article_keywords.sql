ALTER TABLE `article_keywords`
    ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0;

ALTER TABLE `article_keyword_user_map`
    ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0;
