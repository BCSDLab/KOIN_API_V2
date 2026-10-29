ALTER TABLE `order_v2`
    ADD COLUMN `order_number` VARCHAR(10) NULL COMMENT '주문 번호' AFTER `pg_order_id`;

UPDATE `order_v2`
SET `order_number` = LPAD(CONV(`id`, 10, 36), 10, '0')
WHERE `order_number` IS NULL;

ALTER TABLE `order_v2`
    MODIFY COLUMN `order_number` VARCHAR(10) NOT NULL COMMENT '주문 번호',
    ADD UNIQUE KEY `uk_order_v2_order_number` (`order_number`);
