ALTER TABLE `koin`.`order_menu`
    ADD COLUMN menu_option_name VARCHAR(255) NOT NULL COMMENT '메뉴 옵션 이름' AFTER menu_name;
