CREATE TABLE IF NOT EXISTS `koin`.`cart_menu_item`
(
    `id`                              INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `cart_id`                         INT UNSIGNED NOT NULL                COMMENT '장바구니 ID',
    `orderable_shop_menu_id`          INT UNSIGNED NOT NULL                COMMENT '상점 메뉴 ID',
    `orderable_shop_menu_price_id`    INT UNSIGNED NOT NULL                COMMENT '상점 메뉴 가격 ID',
    `quantity`                        INT UNSIGNED NOT NULL                COMMENT '메뉴 수량',
    `is_modified`                     TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '메뉴 정보 변경 여부',
    `created_at`                      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`                      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    PRIMARY KEY (`id`)
);

CREATE INDEX idx_cart_id ON `koin`.`cart_menu_item` (cart_id, orderable_shop_menu_id, orderable_shop_menu_price_id);
