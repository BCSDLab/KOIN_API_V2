ALTER TABLE `shops`
    ADD COLUMN `account_holder` VARCHAR(100) NULL COMMENT '예금주';

ALTER TABLE `order_v2`
    MODIFY COLUMN `pg_order_id` VARCHAR(64) NULL COMMENT 'PG 주문 ID. 직접 계좌이체 주문은 NULL';

ALTER TABLE `payment_v2`
    MODIFY COLUMN `payment_key` VARCHAR(200) NULL COMMENT 'PG 결제 키',
    MODIFY COLUMN `approved_at` TIMESTAMP NULL DEFAULT NULL COMMENT '결제 승인 일시',
    MODIFY COLUMN `receipt` TEXT NULL COMMENT 'PG 영수증',
    ADD COLUMN `bank` VARCHAR(10) NULL COMMENT '주문 당시 은행명',
    ADD COLUMN `account_number` VARCHAR(20) NULL COMMENT '주문 당시 계좌번호',
    ADD COLUMN `account_holder` VARCHAR(100) NULL COMMENT '주문 당시 예금주';
