ALTER TABLE `koin`.`article_attachments`
DROP FOREIGN KEY `article_attachments_ibfk_1`;
ALTER TABLE `koin`.`article_attachments`
    ADD INDEX `FK_ARTICLE_ATTACHMENTS_ON_ARTICLE_ID_idx` (`article_id` ASC) VISIBLE,
DROP INDEX `ux_article_attachment`;
