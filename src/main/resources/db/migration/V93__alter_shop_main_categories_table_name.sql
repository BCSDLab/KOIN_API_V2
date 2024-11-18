RENAME TABLE `shop_main_categories` TO `shop_parent_categories`;

ALTER TABLE `shop_categories`
    CHANGE `main_category_id` `parent_category_id` INT UNSIGNED COMMENT '상위 카테고리 id';

ALTER TABLE `shop_categories`
DROP FOREIGN KEY `FK_SHOP_CATEGORIES_ON_SHOP_MAIN_CATEGORIES`;

ALTER TABLE `shop_categories`
    ADD CONSTRAINT `FK_SHOP_CATEGORIES_ON_SHOP_PARENT_CATEGORIES`
        FOREIGN KEY (`parent_category_id`)
            REFERENCES `shop_parent_categories` (`id`);

ALTER TABLE `shop_parent_categories`
    MODIFY `id` INT UNSIGNED AUTO_INCREMENT COMMENT 'shop_parent_categories 고유 id';
