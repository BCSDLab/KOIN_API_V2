ALTER TABLE `koin`.`shop_menu_category_map` 
DROP COLUMN `is_deleted`,
ADD UNIQUE INDEX `SHOP_MENU_ID_AND_SHOP_MENU_CATEGORY_ID` (`shop_menu_id` ASC, `shop_menu_category_id` ASC);
;
