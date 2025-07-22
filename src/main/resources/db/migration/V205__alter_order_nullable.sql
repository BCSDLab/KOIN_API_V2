ALTER TABLE `koin`.`order_delivery`
    MODIFY COLUMN `to_owner` VARCHAR (50) NULL COMMENT '사장님 전달 메시지',
    MODIFY COLUMN `to_rider` VARCHAR (50) NULL COMMENT '라이더 전달 메시지';

UPDATE `koin`.`order_delivery`
SET `to_owner` = NULL
WHERE TRIM(`to_owner`) = '';

UPDATE `koin`.`order_delivery`
SET `to_rider` = NULL
WHERE TRIM(`to_rider`) = '';
