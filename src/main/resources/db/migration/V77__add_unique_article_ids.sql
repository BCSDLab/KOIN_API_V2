ALTER TABLE `koin`.`koreatech_articles`
    ADD UNIQUE INDEX `article_id_UNIQUE` (`article_id` ASC) VISIBLE;
;

ALTER TABLE `koin`.`koin_articles`
    ADD UNIQUE INDEX `article_id_UNIQUE` (`article_id` ASC) VISIBLE;
;
