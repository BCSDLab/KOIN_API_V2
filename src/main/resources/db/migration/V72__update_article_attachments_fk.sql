ALTER TABLE `koin`.`article_attachments`
DROP FOREIGN KEY `article_attachments_ibfk_1`;
ALTER TABLE `koin`.`article_attachments`
    ADD INDEX `FK_ARTICLE_ATTACHMENTS_ON_ARTICLE_ID_idx` (`article_id` ASC) VISIBLE,
DROP INDEX `ux_article_attachment`;
;
ALTER TABLE `koin`.`article_attachments`
    ADD CONSTRAINT `FK_ARTICLE_ATTACHMENTS_ON_ARTICLE_ID`
        FOREIGN KEY (`article_id`)
            REFERENCES `koin`.`articles` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION;

DROP TABLE `koin`.`old_articles`;
DROP TABLE `koin`.`old_koreatech_articles`;
