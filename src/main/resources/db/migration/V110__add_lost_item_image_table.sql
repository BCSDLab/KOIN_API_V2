CREATE TABLE `lost_item_articles`
(
    `id`              INT UNSIGNED AUTO_INCREMENT NOT NULL comment '고유 id' primary key,
    `article_id`      INT UNSIGNED NOT NULL comment '게시글 id',
    `author_id`       INT UNSIGNED NULL comment '작성자 id',
    `category`        VARCHAR(255) NOT NULL comment '분실물 카테고리',
    `found_place`     VARCHAR(255) NOT NULL comment '습득 장소',
    `found_date`      VARCHAR(255) NOT NULL comment '습득 날짜',
    `is_deleted`      TINYINT(1) NOT NULL DEFAULT 0 comment '게시글 삭제 여부',
    `created_at`      timestamp default CURRENT_TIMESTAMP not null comment '생성 일자',
    `updated_at`      timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '수정 일자',
    CONSTRAINT `lost_item_article_fk_id` FOREIGN KEY (`article_id`) REFERENCES `new_articles` (`id`) ON DELETE CASCADE,
    CONSTRAINT `lost_item_article_author_fk_id` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
);

CREATE TABLE `lost_item_images`
(
    `id`                  INT UNSIGNED AUTO_INCREMENT NOT NULL comment '고유 id' primary key,
    `lost_item_id`        INT UNSIGNED NOT NULL comment '분실물 게시글 id',
    `image_url`           VARCHAR(255) NOT NULL comment '분실물 이미지 url',
    `is_deleted`          TINYINT(1) NOT NULL DEFAULT 0 comment '게시글 삭제 여부',
    `created_at`          timestamp default CURRENT_TIMESTAMP not null comment '생성 일자',
    `updated_at`          timestamp default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '수정 일자',
    CONSTRAINT `lost_item_image_fk_id` FOREIGN KEY (`lost_item_id`) REFERENCES `lost_item_articles` (`id`) ON DELETE CASCADE
);

INSERT INTO `boards` (`id`, `name`)
VALUES (14, '분실물게시판');