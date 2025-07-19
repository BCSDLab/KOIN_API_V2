CREATE TABLE IF NOT EXISTS `koin`.`payment_idempotency_key`
(
    `id`                    INT UNSIGNED        NOT NULL AUTO_INCREMENT COMMENT '결제 멱등키 ID',
    `user_id`               INT UNSIGNED        NOT NULL COMMENT '유저 ID',
    `idempotency_key`       VARCHAR(300)        NOT NULL COMMENT '결제 멱등키',
    `created_at`            TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at`            TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_idempotency_key_user_id` (`user_id`),
    CONSTRAINT `fk_user` FOREIGN KEY (`user_id`) REFERENCES `koin`.`users` (`id`)
);
