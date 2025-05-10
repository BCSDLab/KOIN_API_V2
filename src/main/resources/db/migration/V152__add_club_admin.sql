CREATE TABLE IF NOT EXISTS `koin`.`club_admin`
(
    `id`        INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `club_id`   INT UNSIGNED NOT NULL COMMENT '동아리 고유 ID',
    `user_id`   INT UNSIGNED NOT NULL COMMENT '동아리 관리자 유저 ID',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`club_id`) REFERENCES `koin`.`club` (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `koin`.`users` (`id`) ON DELETE CASCADE
);
