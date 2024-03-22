ALTER TABLE `koin`.`shop_images` 
DROP COLUMN `is_deleted`,
ADD UNIQUE INDEX `SHOP_ID_AND_IMAGE_URL` (`shop_id` ASC, `image_url` ASC);
;
