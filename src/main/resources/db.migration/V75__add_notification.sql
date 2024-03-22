ALTER TABLE `users` ADD COLUMN device_token VARCHAR(255);

CREATE TABLE `notification`(
    id BIGINT AUTO_INCREMENT NOT NULL,
    created_at DATETIME NULL,
    updated_at DATETIME NULL,
    url VARCHAR(255) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message VARCHAR(255) NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    `type` VARCHAR(255) NOT NULL,
    users_id INT UNSIGNED NOT NULL,
    is_read TINYINT(1) NOT NULL,
    PRIMARY KEY(`id`),
    CONSTRAINT `FK_NOTIFICATION_ON_USER FOREIGN KEY` FOREIGN KEY (`users_id`) REFERENCES `users` (`id`)
);
