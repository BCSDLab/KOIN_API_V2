ALTER TABLE `article_keywords`
    ADD COLUMN `category` VARCHAR(20) NOT NULL DEFAULT 'KOREATECH' AFTER `keyword`;

UPDATE `article_keywords`
SET `category` = 'KOREATECH';

ALTER TABLE `article_keywords`
    DROP INDEX `keyword`,
    ADD UNIQUE KEY `uk_article_keywords_keyword_category` (`keyword`, `category`);
