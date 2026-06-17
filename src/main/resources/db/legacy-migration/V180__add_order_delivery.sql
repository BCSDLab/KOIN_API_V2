CREATE TABLE IF NOT EXISTS `koin`.`order_delivery`
(
    `order_id`          VARCHAR(64)     NOT NULL COMMENT '주문 ID',
    `address`           VARCHAR(100)    NOT NULL COMMENT '배달 주소',
    `to_owner`          VARCHAR(50)     NOT NULL COMMENT '사장님 전달 메시지',
    `to_rider`          VARCHAR(50)     NOT NULL COMMENT '라이더 전달 메시지',
    `delivery_tip`      INT UNSIGNED    NOT NULL COMMENT '배달비',
    PRIMARY KEY (order_id),
    CONSTRAINT `fk_order_delivery_order` FOREIGN KEY (`order_id`) REFERENCES `koin`.`order` (`id`)
);
