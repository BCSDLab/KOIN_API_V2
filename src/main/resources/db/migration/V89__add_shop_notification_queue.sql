CREATE TABLE `shop_notification_queue`
(
    id                INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT 'shop_notification_queue 고유 id',
    shop_id           INT UNSIGNED NOT NULL COMMENT '상점 ID',
    user_id           INT UNSIGNED NOT NULL COMMENT '사용자 ID',
    notification_time TIMESTAMP NOT NULL COMMENT '알림 시간',
    CONSTRAINT `FK_SHOP_NOTIFICATION_QUEUE_ON_SHOPS`
        FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`),
    CONSTRAINT `FK_SHOP_NOTIFICATION_QUEUE_ON_USERS`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);
