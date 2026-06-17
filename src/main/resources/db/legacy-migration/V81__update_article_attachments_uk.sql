ALTER TABLE `koin`.`article_attachments`
    ADD UNIQUE KEY ux_article_attachment (`article_id`, `hash`);
