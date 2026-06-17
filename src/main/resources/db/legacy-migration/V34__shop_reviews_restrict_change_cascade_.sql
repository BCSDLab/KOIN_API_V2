ALTER TABLE `koin`.`shop_reviews`
    DROP FOREIGN KEY `shop_reviews_ibfk_1`,
    DROP FOREIGN KEY `shop_reviews_ibfk_2`;

ALTER TABLE `koin`.`shop_reviews`
    ADD CONSTRAINT `shop_reviews_ibfk_1`
        FOREIGN KEY (`reviewer_id`)
        REFERENCES `koin`.`users` (`id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `shop_reviews_ibfk_2`
        FOREIGN KEY (`shop_id`)
        REFERENCES `koin`.`shops` (`id`)
        ON DELETE CASCADE;
