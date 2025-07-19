CREATE TABLE IF NOT EXISTS `koin`.`cart_menu_item_option`
(
    `id`                              INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `cart_menu_item_id`               INT UNSIGNED NOT NULL                COMMENT '장바구니 메뉴 ID',
    `orderable_shop_menu_option_id`   INT UNSIGNED NOT NULL                COMMENT '상점 메뉴 옵션 ID',
    `option_name`                     VARCHAR(255) NOT NULL                COMMENT '옵션 이름',
    `option_price`                    INT UNSIGNED NOT NULL                COMMENT '옵션 가격',
    `quantity`                        INT UNSIGNED NOT NULL                COMMENT '옵션 수량',
    `is_modified`                     TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '옵션 정보 변경 여부',
    `created_at`                      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`                      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    PRIMARY KEY (`id`)
);

CREATE INDEX idx_cart_menu_item_id ON `koin`.`cart_menu_item_option` (cart_menu_item_id, orderable_shop_menu_option_id);
