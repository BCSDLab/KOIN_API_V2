CREATE TABLE `notification_keywords`
(
    `id`           INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `keyword`      VARCHAR(50) NOT NULL UNIQUE,
    `last_used_at` TIMESTAMP NULL,
    `created_at`   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE `notification_keyword_user_map`
(
    `keyword_id` INT UNSIGNED NOT NULL,
    `user_id`    INT UNSIGNED NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`keyword_id`, `user_id`),
    FOREIGN KEY (`keyword_id`) REFERENCES `keywords` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
);
