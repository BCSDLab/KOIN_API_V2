CREATE TABLE IF NOT EXISTS `koin`.`order_v2`
(
    `id`                            INT UNSIGNED        NOT NULL AUTO_INCREMENT COMMENT '주문 고유 ID',
    `pg_order_id`                   VARCHAR(64)         NOT NULL COMMENT 'PG 주문 ID',
    `user_id`                       INT UNSIGNED        NOT NULL COMMENT '유저 고유 ID',
    `phone_number`                  VARCHAR(20)         NOT NULL COMMENT '전화번호',
    `orderable_shop_id`             INT UNSIGNED        NOT NULL COMMENT '주문 가능 상점 ID',
    `orderable_shop_name`           VARCHAR(255)        NOT NULL COMMENT '주문 가능 상점 이름',
    `orderable_shop_address`        TEXT                NOT NULL COMMENT '주문 가능 상점 주소',
    `orderable_shop_address_detail` TEXT                NULL COMMENT '주문 가능 상점 상세 주소',
    `total_product_price`           INT UNSIGNED        NOT NULL COMMENT '상품 총 금액',
    `discount_amount`               INT UNSIGNED        DEFAULT 0 NOT NULL COMMENT '할인 금액',
    `total_price`                   INT UNSIGNED        NOT NULL COMMENT '주문 총 금액',
    `order_type`                    VARCHAR(10)         NOT NULL COMMENT '주문 타입',
    `status`                        VARCHAR(10)         NOT NULL COMMENT '주문 상태',
    `canceled_at`                   TIMESTAMP           NULL COMMENT '취소 일시',
    `canceled_reason`               VARCHAR(200)        NULL COMMENT '취소 사유',
    `is_deleted`                    TINYINT(1)          NOT NULL DEFAULT '0' COMMENT '삭제 여부',
    `created_at`                    TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at`                    TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (`id`)
);

CREATE INDEX `idx_user_id` ON `koin`.`order` (`user_id`);
CREATE INDEX `idx_orderable_shop_id` ON `koin`.`order` (`orderable_shop_id`);
