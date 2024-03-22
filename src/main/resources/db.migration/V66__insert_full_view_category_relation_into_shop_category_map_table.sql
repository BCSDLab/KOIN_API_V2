INSERT IGNORE INTO `koin`.`shop_category_map` (`shop_id`, `shop_category_id`)
SELECT `id`, 1
FROM `koin`.`shops`