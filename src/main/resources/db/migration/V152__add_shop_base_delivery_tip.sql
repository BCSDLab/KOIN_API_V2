CREATE TABLE IF NOT EXISTS `koin`.`shop_base_delivery_tip`
(
    `id`            INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `shop_id`       INT UNSIGNED NOT NULL COMMENT '상점 ID',
    `order_amount`  INT          NOT NULL COMMENT '주문 금액 기준',
    `fee`           INT          NOT NULL COMMENT '배달비',
    `is_deleted`    tinyint(1)   NOT NULL DEFAULT 0 COMMENT '삭제 여부',
    `created_at`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    PRIMARY KEY (`id`)
);

CREATE INDEX idx_shop_base_delivery_tip_shop_id ON shop_base_delivery_tip (shop_id);
CREATE INDEX idx_shop_base_delivery_tip_01 ON shop_base_delivery_tip (shop_id, order_amount, fee);
CREATE INDEX idx_shop_base_delivery_tip_02 ON shop_base_delivery_tip (shop_id, is_deleted, fee);
