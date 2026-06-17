CREATE TABLE IF NOT EXISTS `koin`.`order_menu_option`
(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '메뉴 옵션 ID',
    `option_name` VARCHAR(255) NOT NULL COMMENT '옵션 이름',
    `option_price` INT UNSIGNED NOT NULL COMMENT '옵션 가격',
    `quantity` INT UNSIGNED NOT NULL COMMENT '옵션 수량',
    `order_menu_id` INT UNSIGNED NOT NULL COMMENT '주문 메뉴 ID',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_order_menu_option_menu` FOREIGN KEY (`order_menu_id`) REFERENCES `koin`.`order_menu` (`id`)
);
