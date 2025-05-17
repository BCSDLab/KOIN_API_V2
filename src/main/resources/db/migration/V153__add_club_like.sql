CREATE TABLE IF NOT EXISTS `koin`.`club_like`
(
    `id`                 INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `club_id`            INT UNSIGNED NOT NULL COMMENT '동아리 고유 ID',
    `user_id`            INT UNSIGNED NOT NULL COMMENT '유저 ID',
    `created_at`         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`club_id`) REFERENCES `koin`.`club` (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `koin`.`users` (`id`) ON DELETE CASCADE
);
