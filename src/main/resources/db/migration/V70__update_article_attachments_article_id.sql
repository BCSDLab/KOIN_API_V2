-- articles_attachments.article_id 최신화
SET FOREIGN_KEY_CHECKS = 0;
SET SQL_SAFE_UPDATES = 0;

UPDATE `koin`.`article_attachments` a
    JOIN `koin`.`new_articles` na ON a.article_id = na.old_id
    SET a.article_id = na.id;

SET FOREIGN_KEY_CHECKS = 1;
SET SQL_SAFE_UPDATES = 1;

-- new_articles에서 old_id 컬럼 삭제
ALTER TABLE `koin`.`new_articles`
DROP COLUMN old_id;
