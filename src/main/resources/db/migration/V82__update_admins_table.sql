DROP TABLE IF EXISTS `koin`.`admins`;

CREATE TABLE `koin`.`admins`
(
    `user_id`          INT UNSIGNED NOT NULL COMMENT 'user 고유 id',
    `team_type`        VARCHAR(255) NOT NULL COMMENT '팀 타입',
    `track_type`       VARCHAR(255) NOT NULL COMMENT '트랙 타입',
    `can_create_admin` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '어드민 계정 생성 권한',
    `super_admin`      TINYINT(1) NOT NULL DEFAULT 0 COMMENT '슈퍼 어드민 권한',
    PRIMARY KEY (`user_id`),
    CONSTRAINT FK_ADMIN_ON_USER FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
);
