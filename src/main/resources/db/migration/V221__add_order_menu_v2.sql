CREATE TABLE IF NOT EXISTS `koin`.`order_menu`
(
    `id`                                INT UNSIGNED    NOT NULL AUTO_INCREMENT COMMENT '주문 메뉴 ID',
    `order_id`                          INT UNSIGNED    NOT NULL COMMENT '주문 ID',
    `orderable_shop_menu_id`            INT UNSIGNED    NOT NULL COMMENT '주문 가능 상점 메뉴 ID',
    `orderable_shop_menu_price_id`      INT UNSIGNED    NOT NULL COMMENT '주문 가능 상점 메뉴 가격 ID',
    `name`                              VARCHAR(255)    NOT NULL COMMENT '메뉴 이름',
    `price_name`                        VARCHAR(255)    NULL COMMENT '가격 이름',
    `price`                             INT UNSIGNED    NOT NULL COMMENT '가격',
    `quantity`                          INT UNSIGNED    NOT NULL COMMENT '수량',
    `is_deleted`                        TINYINT(1)      NOT NULL DEFAULT '0' COMMENT '삭제 여부',
    `created_at`                        TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at`                        TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_order_menu_order` FOREIGN KEY (`order_id`) REFERENCES `koin`.`order` (`id`)
);
