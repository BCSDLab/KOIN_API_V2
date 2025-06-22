CREATE TABLE IF NOT EXISTS `koin`.`order_pack`
(
    `order_id`          VARCHAR(64)     NOT NULL COMMENT '주문 ID',
    `to_owner`          VARCHAR(50)     NOT NULL COMMENT '사장님 전달 메시지',
    PRIMARY KEY (order_id),
    CONSTRAINT `fk_order_pack_order` FOREIGN KEY (`order_id`) REFERENCES `koin`.`order` (`id`)
);
