CREATE TABLE `koin`.`admins`
(
    `user_id`       INT UNSIGNED NOT NULL COMMENT 'user 고유 id',
    `team_name`     VARCHAR(10) NOT NULL COMMENT '팀 이름',
    `track_name`    VARCHAR(20) NOT NULL COMMENT '트랙 이름',
    `create_admin`  TINYINT(1) NOT NULL DEFAULT 0 COMMENT '어드민 계정 생성 권한',
    `super_admin` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '슈퍼 어드민 권한',
    PRIMARY KEY (`user_id`),
    CONSTRAINT FK_ADMIN_ON_USER FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
)
