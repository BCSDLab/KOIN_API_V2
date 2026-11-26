ALTER TABLE `order_v2`
    MODIFY COLUMN `pg_order_id` VARCHAR(64) NULL COMMENT 'PG 주문 ID. 직접 계좌이체 주문은 NULL';
