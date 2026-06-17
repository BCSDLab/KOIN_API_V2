ALTER TABLE `koin`.`order_takeout`
    ADD COLUMN `provide_cutlery` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '수저, 포크 수령 여부';

ALTER TABLE `koin`.`order_delivery`
    ADD COLUMN `provide_cutlery` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '수저, 포크 수령 여부';
