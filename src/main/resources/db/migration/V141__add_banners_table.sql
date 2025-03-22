CREATE TABLE IF NOT EXISTS `koin`.`banners`
(
    `id`                    INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `title`                 VARCHAR(255) NOT NULL COMMENT '배너 이름',
    `banner_category_id`    INT UNSIGNED NOT NULL COMMENT '배너 카테고리 ID',
    `priority`              INT UNSIGNED NOT NULL COMMENT '배너 우선 순위',
    `image_url`             TEXT NOT NULL COMMENT '배너 이미지 URL',
    `web_redirect_link`     TEXT NULL COMMENT '웹 리다이렉션 URL',
    `android_redirect_link` TEXT NULL COMMENT '안드로이드 리다이렉션 URL',
    `ios_redirect_link`     TEXT NULL COMMENT 'IOS 리다이렉션 URL',
    `is_active`             TINYINT(1) NOT NULL COMMENT '배너 활성화 여부',
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    updated_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`banner_category_id`) REFERENCES `koin`.`banner_category` (`id`)
);
