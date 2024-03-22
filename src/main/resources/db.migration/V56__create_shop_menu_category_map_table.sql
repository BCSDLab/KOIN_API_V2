CREATE TABLE `koin`.`shop_menu_category_map` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'shop_menu_category_map 고유 id',
  `shop_menu_id` int unsigned NOT NULL COMMENT 'shop_menus 고유 id',
  `shop_menu_category_id` int unsigned NOT NULL COMMENT 'shop_menu_categories 고유 id',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8
COLLATE = utf8_bin;

INSERT INTO `koin`.`shop_menu_category_map` (`shop_menu_id`, `shop_menu_category_id`)
SELECT sm.`id`, smc.`id`
FROM `koin`.`shop_menus` sm
    INNER JOIN `koin`.`shop_menu_categories` smc
        ON sm.`shop_id` = smc.`shop_id` AND smc.`name` = '대표 메뉴'
WHERE sm.`is_deleted` = 0 AND smc.`is_deleted` = 0;