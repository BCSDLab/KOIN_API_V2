CREATE TABLE IF NOT EXISTS `koin`.`orderable_shop`
(
    `id`            INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `shop_id`       INT UNSIGNED NOT NULL UNIQUE COMMENT '상점 ID',
    `delivery`      tinyint(1)   NOT NULL DEFAULT 1 COMMENT '배달 가능 여부',
    `takeout`       tinyint(1)   NOT NULL DEFAULT 1 COMMENT '포장 가능 여부',
    `service_event` tinyint(1)   NOT NULL DEFAULT 0 COMMENT '서비스 증정 여부',
    `minimum_order_amount`       INT UNSIGNED NOT NULL COMMENT '최소 주문 금액',
    `is_deleted`    tinyint(1)   NOT NULL DEFAULT 0 COMMENT '삭제 여부',
    `created_at`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    PRIMARY KEY (`id`)
);

CREATE INDEX idx_orderable_shop_shop_id ON orderable_shop (shop_id);
CREATE INDEX idx_orderable_shop_minimum_order_amount ON orderable_shop (minimum_order_amount, shop_id);
CREATE INDEX idx_orderable_shop_filter_01 ON orderable_shop (delivery, takeout, shop_id);
CREATE INDEX idx_orderable_shop_filter_02 ON orderable_shop (minimum_order_amount, delivery, takeout, shop_id);
