ALTER TABLE `shop_categories`
    ADD COLUMN `main_category_id` INT UNSIGNED COMMENT '메인 카테고리 id',
    ADD CONSTRAINT `FK_SHOP_CATEGORIES_ON_SHOP_MAIN_CATEGORIES`
    FOREIGN KEY (`main_category_id`)
    REFERENCES `shop_main_categories` (`id`);
