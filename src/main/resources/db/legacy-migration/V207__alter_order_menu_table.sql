ALTER TABLE `koin`.`order_menu`
    ADD COLUMN `menu_price_name` VARCHAR(255) NULL COMMENT '메뉴 가격 이름';

ALTER TABLE `koin`.`order_menu`
    MODIFY COLUMN `menu_option_name` VARCHAR(255) NULL COMMENT '메뉴 옵션 이름';

UPDATE `koin`.`order_menu` SET
    `menu_option_name` = NULL;
