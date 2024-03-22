ALTER TABLE `koin`.`shop_menus`
DROP COLUMN `price_type`,
ADD COLUMN `description` VARCHAR(255) NULL COMMENT '메뉴 구성' AFTER `name`,
ADD COLUMN `is_hidden` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '숨김 여부' AFTER `description`;
