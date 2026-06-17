ALTER TABLE `lost_item_articles`
DROP FOREIGN KEY `lost_item_article_author_fk_id`;

ALTER TABLE `lost_item_articles`
ADD CONSTRAINT `lost_item_article_author_fk_id` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;
