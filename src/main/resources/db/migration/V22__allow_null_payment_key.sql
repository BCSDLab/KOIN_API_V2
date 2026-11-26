ALTER TABLE `payment_v2`
    MODIFY COLUMN `payment_key` VARCHAR(200) NULL COMMENT 'PG 결제 키';
