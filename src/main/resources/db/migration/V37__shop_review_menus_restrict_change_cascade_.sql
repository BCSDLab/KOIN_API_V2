ALTER TABLE `koin`.`shop_review_menus`
    DROP FOREIGN KEY `shop_review_menus_ibfk_1`;

ALTER TABLE `koin`.`shop_review_menus`
    ADD CONSTRAINT `shop_review_menus_ibfk_1`
        FOREIGN KEY (`review_id`)
        REFERENCES `koin`.`shop_reviews` (`id`)
        ON DELETE CASCADE;
