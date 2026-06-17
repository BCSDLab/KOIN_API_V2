ALTER TABLE `article_keywords`
    ADD COLUMN `is_filtered` TINYINT(1) NOT NULL DEFAULT 0;

UPDATE `article_keywords`
SET `is_filtered` = 0;
