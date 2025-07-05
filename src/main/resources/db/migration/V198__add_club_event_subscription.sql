CREATE TABLE IF NOT EXISTS `koin`.`club_event_subscription`
(
    `id`                    INT UNSIGNED        NOT NULL AUTO_INCREMENT COMMENT '동아리 모집 고유 ID',
    `event_id`              INT UNSIGNED        NOT NULL COMMENT '동아리 이벤트 고유 ID',
    `user_id`               INT UNSIGNED        NOT NULL COMMENT '유저 고유 ID',
    `created_at`            TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at`            TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`event_id`) REFERENCES `koin`.`club_event` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`user_id`) REFERENCES `koin`.`users` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `uniq_event_user` (`event_id`, `user_id`)
    );
