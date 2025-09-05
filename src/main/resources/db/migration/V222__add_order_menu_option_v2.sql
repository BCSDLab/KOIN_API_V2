CREATE TABLE IF NOT EXISTS `koin`.`order_menu_option`
(
    `id`                                       INT UNSIGNED    NOT NULL    AUTO_INCREMENT COMMENT '메뉴 옵션 ID',
    `order_menu_id`                            INT UNSIGNED    NOT NULL    COMMENT '주문 상점 메뉴 ID',
    `orderable_shop_menu_option_group_id`      INT UNSIGNED    NOT NULL    COMMENT '주문 가능 상점 메뉴 옵션 그룹 ID',
    `orderable_shop_menu_option_id`            INT UNSIGNED    NOT NULL    COMMENT '주문 가능 상점 메뉴 옵션 ID',
    `group_name`                               VARCHAR(255)    NOT NULL    COMMENT '그룹 이름',
    `name`                                     VARCHAR(255)    NOT NULL    COMMENT '옵션 이름',
    `price`                                    INT UNSIGNED    NOT NULL    COMMENT '옵션 가격',
    `quantity`                                 INT UNSIGNED    NOT NULL    COMMENT '옵션 수량',
    `is_deleted`                               TINYINT(1)      NOT NULL    DEFAULT '0' COMMENT '삭제 여부',
    `created_at`                               TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at`                               TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_order_menu_option_menu` FOREIGN KEY (`order_menu_id`) REFERENCES `koin`.`order_menu` (`id`)
);
