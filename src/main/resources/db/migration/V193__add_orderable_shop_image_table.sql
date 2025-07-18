CREATE TABLE IF NOT EXISTS `koin`.`orderable_shop_image`
(
    `id`                     INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `orderable_shop_id`      INT UNSIGNED NOT NULL                COMMENT '주문 가능 상점 ID',
    `image_url`              VARCHAR(255) NOT NULL                COMMENT '이미지 URL',
    `is_thumbnail`           TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '대표 이미지 여부',
    `is_deleted`             TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '삭제 여부',
    `created_at`             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    PRIMARY KEY (`id`)
);

CREATE INDEX idx_images_on_orderable_shop_id ON `koin`.`orderable_shop_image` (orderable_shop_id);
CREATE INDEX idx_thumbnail_images ON `koin`.`orderable_shop_image` (orderable_shop_id, is_thumbnail);
