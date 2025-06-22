CREATE TABLE IF NOT EXISTS `koin`.`payment`
(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `payment_key` VARCHAR(200) NOT NULL COMMENT '결제 키',
    `amount` INT UNSIGNED NOT NULL COMMENT '결제 금액',
    `status` VARCHAR(30) NOT NULL COMMENT '결제 상태',
    `method` VARCHAR(30) NOT NULL COMMENT '결제 수단',
    `requested_at` TIMESTAMP NOT NULL COMMENT '결제 요청 일시',
    `approved_at` TIMESTAMP NOT NULL COMMENT '결제 승인 일시',
    `order_id` VARCHAR(64) NOT NULL COMMENT '주문 번호',
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_payment_key (`id`),
    CONSTRAINT fk_payment_order FOREIGN KEY (`order_id`) REFERENCES `koin`.`order` (`id`)
);
