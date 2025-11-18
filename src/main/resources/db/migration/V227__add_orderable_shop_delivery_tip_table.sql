CREATE TABLE IF NOT EXISTS `koin`.`orderable_shop_delivery_tip`
(
    `id`                       INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `orderable_shop_id`        INT UNSIGNED NOT NULL COMMENT '주문 가능 상점 ID',
    `campus_delivery_tip`      INT UNSIGNED NOT NULL COMMENT '교내 배달팁',
    `off_campus_delivery_tip`  INT UNSIGNED NOT NULL COMMENT '교외 배달팁',
    `created_at`               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`               TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_orderable_shop_id` (`orderable_shop_id`),
    CONSTRAINT `fk_orderable_shop_delivery_tip_orderable_shop`
        FOREIGN KEY (`orderable_shop_id`) REFERENCES `orderable_shop` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE
)
