ALTER TABLE `koin`.`shops`
    ADD COLUMN `bank` VARCHAR(10) NULL DEFAULT NULL AFTER `hit`,
    ADD COLUMN `account_number` VARCHAR(100) NULL DEFAULT NULL AFTER `bank`;