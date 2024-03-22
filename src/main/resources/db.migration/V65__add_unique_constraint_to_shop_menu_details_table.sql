ALTER TABLE `koin`.`shop_menu_details` 
DROP COLUMN `is_deleted`,
ADD UNIQUE INDEX `SHOP_MENU_ID_AND_OPTION_AND_PRICE` (`shop_menu_id` ASC, `option` ASC, `price` ASC);
;
