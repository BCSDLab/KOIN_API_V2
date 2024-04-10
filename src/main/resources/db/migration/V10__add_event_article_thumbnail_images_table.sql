CREATE TABLE if not exists `event_article_thumbnail_images`
(
    id             INT UNSIGNED AUTO_INCREMENT NOT NULL,
    event_id        INT UNSIGNED NOT NULL,
    thumbnail_image VARCHAR(255),
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `FK_EVENT_ARTICLE_THUMBNAIL_IMAGES_ON_EVENT_ARTICLES` FOREIGN KEY (`event_id`) REFERENCES `event_articles` (`id`)
);
