ALTER TABLE `koin`.`shop_review_reports`
    DROP FOREIGN KEY `shop_review_reports_ibfk_1`,
    DROP FOREIGN KEY `shop_review_reports_ibfk_2`;

ALTER TABLE `koin`.`shop_review_reports`
    ADD CONSTRAINT `shop_review_reports_ibfk_1`
        FOREIGN KEY (`review_id`)
        REFERENCES `koin`.`shop_reviews` (`id`)
        ON DELETE CASCADE,
    ADD CONSTRAINT `shop_review_reports_ibfk_2`
        FOREIGN KEY (`user_id`)
        REFERENCES `koin`.`users` (`id`)
        ON DELETE CASCADE;
