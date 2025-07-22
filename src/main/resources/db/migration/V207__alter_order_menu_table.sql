ALTER TABLE `koin`.`order_menu`
    DROP COLUMN `menu_option_name`;

ALTER TABLE `koin`.`order_menu`
    ADD COLUMN `menu_price_name` VARCHAR(255) NULL COMMENT '메뉴 가격 이름';
