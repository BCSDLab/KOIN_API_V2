ALTER TABLE `koin`.`shop_category_map` 
DROP COLUMN `is_deleted`,
ADD UNIQUE INDEX `SHOP_ID_AND_SHOP_CATEGORY_ID` (`shop_id` ASC, `shop_category_id` ASC);
;
