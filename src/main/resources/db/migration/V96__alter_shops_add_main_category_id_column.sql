ALTER TABLE `shops`
    ADD COLUMN `main_category_id` INT UNSIGNED COMMENT '메인 카테고리 id',
    ADD CONSTRAINT `FK_SHOPS_ON_SHOP_CATEGORIES`
    FOREIGN KEY (`main_category_id`)
    REFERENCES `shop_categories` (`id`);
