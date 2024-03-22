CREATE TABLE `owner_shop_attachment`
(
    `id`         int unsigned NOT NULL AUTO_INCREMENT,
    `shop_id`    int unsigned,
    `owner_id`   int unsigned NOT NULL,
    `url`        TEXT      NOT NULL,
    `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
    `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT `owner_shop_attachment_fk_shop_id` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`),
    CONSTRAINT `owner_shop_attachment_fk_owner_id` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`)
) ENGINE = InnoDB
DEFAULT CHARSET = utf8
COLLATE = utf8_bin;