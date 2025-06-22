CREATE TABLE IF NOT EXISTS `koin`.`order_menu`
(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '주문 메뉴 ID',
    `menu_name` VARCHAR(255) NOT NULL COMMENT '메뉴 이름',
    `quantity` INT UNSIGNED NOT NULL COMMENT '수량',
    `order_id` VARCHAR(64) NOT NULL COMMENT '주문 ID',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_order_menu_order` FOREIGN KEY (`order_id`) REFERENCES `koin`.`order` (`id`)
);
