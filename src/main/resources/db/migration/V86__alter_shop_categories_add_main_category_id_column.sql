ALTER TABLE `shop_categories`
    ADD COLUMN `main_category_id` INT UNSIGNED COMMENT '메인 카테고리 id',
    ADD CONSTRAINT `FK_SHOP_CATEGORIES_ON_SHOP_MAIN_CATEGORIES`
    FOREIGN KEY (`main_category_id`)
    REFERENCES `shop_main_categories` (`id`);

UPDATE shop_categories
SET main_category_id = 1
WHERE id BETWEEN 2 AND 10;

UPDATE shop_categories
SET main_category_id = 2
WHERE id = 11;

UPDATE shop_categories
SET main_category_id = 3
WHERE id = 12;