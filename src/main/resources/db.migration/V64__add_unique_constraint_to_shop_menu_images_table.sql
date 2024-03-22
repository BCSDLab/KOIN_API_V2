ALTER TABLE `koin`.`shop_menu_images` 
DROP COLUMN `is_deleted`,
ADD UNIQUE INDEX `SHOP_MENU_ID_AND_IMAGE_URL` (`shop_menu_id` ASC, `image_url` ASC);
;
