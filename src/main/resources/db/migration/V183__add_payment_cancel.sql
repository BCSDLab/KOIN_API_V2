CREATE TABLE IF NOT EXISTS `koin`.`payment_cancel`
(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '결제 취소 ID',
    `transaction_key` VARCHAR(64) NOT NULL COMMENT '취소 트랜잭션 키',
    `cancel_reason` VARCHAR(200) NOT NULL COMMENT '취소 사유',
    `cancel_amount` INT UNSIGNED NOT NULL COMMENT '취소 금액',
    `canceled_at` TIMESTAMP NOT NULL COMMENT '취소 일시',
    `payment_id` INT UNSIGNED NOT NULL COMMENT '결제 ID',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_payment_cancel_payment` FOREIGN KEY (`payment_id`) REFERENCES `koin`.`payment` (`id`)
);
