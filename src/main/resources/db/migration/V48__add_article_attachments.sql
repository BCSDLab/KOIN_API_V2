CREATE TABLE `article_attachments` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `article_id` INT UNSIGNED NOT NULL,
    `hash` BINARY(32) NOT NULL,
    `url` TEXT NOT NULL,
    `name` TEXT NOT NULL,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT '0',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY ux_article_attachment (`article_id`, `hash`),
    FOREIGN KEY (`article_id`) REFERENCES `koreatech_articles` (`id`) ON DELETE CASCADE
);
