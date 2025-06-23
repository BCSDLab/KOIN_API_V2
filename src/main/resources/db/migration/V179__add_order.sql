CREATE TABLE IF NOT EXISTS `koin`.`order`
(
    `id` VARCHAR(64) NOT NULL COMMENT '주문 ID',

    `order_type` VARCHAR(10) NOT NULL COMMENT '주문 타입',
    `phone_number` VARCHAR(20) NOT NULL COMMENT '주문자 전화번호',
    `total_product_price` INT UNSIGNED COMMENT '상품 총 금액',
    `total_price` INT UNSIGNED COMMENT '주문 총 금액',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT FALSE COMMENT '삭제 여부',
    `orderable_shop_id` INT UNSIGNED COMMENT '주문한 상점 ID',
    `user_id` INT UNSIGNED COMMENT '주문자 사용자 ID',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_order_shop` FOREIGN KEY (`orderable_shop_id`) REFERENCES `koin`.`orderable_shop` (`id`),
    CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `koin`.`users` (`id`)
);
