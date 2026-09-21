ALTER TABLE `order`
    DROP FOREIGN KEY `fk_order_user`,
    ADD CONSTRAINT `fk_order_user_withdrawal`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL;

ALTER TABLE `payment_idempotency_key`
    DROP FOREIGN KEY `fk_user`,
    ADD CONSTRAINT `fk_payment_idempotency_key_user`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE;
