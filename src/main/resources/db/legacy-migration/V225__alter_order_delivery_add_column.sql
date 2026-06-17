ALTER TABLE `koin`.`order_delivery_v2`
    ADD COLUMN `latitude` DECIMAL(10, 8) NULL COMMENT '위도' AFTER `address_detail`,
    ADD COLUMN `longitude` DECIMAL(11, 8) NULL COMMENT '경도' AFTER `latitude`;
