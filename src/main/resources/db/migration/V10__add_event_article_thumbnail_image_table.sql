CREATE TABLE `event_article_thumbnail_images`
(
    id              INT UNSIGNED NOT NULL,
    event_id        INT UNSIGNED NOT NULL,
    thumbnail_image VARCHAR(255),
    created_at      datetime NOT NULL,
    updated_at      datetime NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `FK_EVENT_ARTICLE_THUMBNAIL_IMAGES_ON_EVENT_ARTICLES` FOREIGN KEY (`event_id`) REFERENCES `event_articles` (`id`)
);
