ALTER TABLE `koin`.`shop_review_images`
    DROP FOREIGN KEY `shop_review_images_ibfk_1`;

ALTER TABLE `koin`.`shop_review_images`
    ADD CONSTRAINT `shop_review_images_ibfk_1`
        FOREIGN KEY (`review_id`)
        REFERENCES `koin`.`shop_reviews` (`id`)
        ON DELETE CASCADE;
