CREATE TABLE `notification_detail_subscribe`
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    created_at     datetime              NOT NULL,
    updated_at     datetime              NOT NULL,
    subscribe_type VARCHAR(255)          NOT NULL,
    detail_subscribe_type VARCHAR(255)          NOT NULL,
    user_id        INT UNSIGNED          NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT `FK_NOTIFICATION_DETAIL_SUBSCRIBE_ON_USER` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);
