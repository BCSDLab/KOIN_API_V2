CREATE TABLE IF NOT EXISTS `koin`.`orderable_shop_menu_price`
(
    `id`                     INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `orderable_shop_menu_id` INT UNSIGNED NOT NULL                COMMENT '메뉴 ID',
    `name`                   VARCHAR(255) NULL                    COMMENT '가격 옵션 이름 (ex: 대, 중, 소)',
    `price`                  INT UNSIGNED NOT NULL                COMMENT '가격',
    `is_deleted`             TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '삭제 여부',
    `created_at`             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    PRIMARY KEY (`id`)
);

CREATE INDEX idx_price_on_menu_id ON `koin`.`orderable_shop_menu_price` (orderable_shop_menu_id);
