CREATE TABLE IF NOT EXISTS `koin`.`club_qna`
(
    `id`                 INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `club_id`            INT UNSIGNED NOT NULL COMMENT '동아리 고유 ID',
    `user_id`            INT UNSIGNED NULL COMMENT '유저 ID',
    `parent_id`          INT UNSIGNED NULL COMMENT '부모 qna ID',
    `content`            VARCHAR(255) NOT NULL COMMENT '내용',
    `is_deleted`         TINYINT(1) NOT NULL DEFAULT 0 COMMENT '삭제 여부',
    `created_at`         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`club_id`) REFERENCES `koin`.`club` (`id`),
    FOREIGN KEY (`user_id`) REFERENCES `koin`.`users` (`id`) ON DELETE SET NULL,
    FOREIGN KEY (`parent_id`) REFERENCES `koin`.`club_qna` (`id`)
);
