CREATE TABLE IF NOT EXISTS `koin`.`koin_notice`
(
    `id`    INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `article_id` INT UNSIGNED NOT NULL COMMENT '게시글 고유 ID',
    `admin_id` INT UNSIGNED NOT NULL COMMENT '어드민 고유 ID',
    `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
    `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`article_id`) REFERENCES `koin`.`new_articles` (`id`),
    FOREIGN KEY (`admin_id`) REFERENCES `koin`.`admins` (`user_id`)
)
