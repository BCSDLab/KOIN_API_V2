ALTER TABLE `koin`.`user_delivery_address`
    ADD COLUMN `address` text NULL COMMENT '상세 정보를 제외한 기본 주소' AFTER `building`;
