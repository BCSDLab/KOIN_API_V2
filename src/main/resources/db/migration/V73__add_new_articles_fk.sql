ALTER TABLE `koin`.`articles`
    ADD INDEX `FK_ARTICLES_ON_BOARD_ID_idx` (`board_id` ASC) VISIBLE;
;
ALTER TABLE `koin`.`articles`
    ADD CONSTRAINT `FK_ARTICLES_ON_BOARD_ID`
        FOREIGN KEY (`board_id`)
            REFERENCES `koin`.`boards` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION;


ALTER TABLE `koin`.`koin_articles`
    ADD INDEX `FK_KOIN_ARTICLES_ON_ARTICLE_ID_idx` (`article_id` ASC) VISIBLE;
;
ALTER TABLE `koin`.`koin_articles`
    ADD CONSTRAINT `FK_KOIN_ARTICLES_ON_ARTICLE_ID`
        FOREIGN KEY (`article_id`)
            REFERENCES `koin`.`articles` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION;


ALTER TABLE `koin`.`koreatech_articles`
    ADD INDEX `FK_KOREATECH_ARTICLES_ON_ARTICLE_ID_idx` (`article_id` ASC) VISIBLE;
;
ALTER TABLE `koin`.`koreatech_articles`
    ADD CONSTRAINT `FK_KOREATECH_ARTICLES_ON_ARTICLE_ID`
        FOREIGN KEY (`article_id`)
            REFERENCES `koin`.`articles` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION;
