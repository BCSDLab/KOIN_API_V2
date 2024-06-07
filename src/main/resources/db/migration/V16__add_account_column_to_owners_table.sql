ALTER TABLE `koin`.`owners`
    ADD COLUMN `account` VARCHAR(255) NULL COMMENT '사장님 전화번호(“-“ 제거, unique)',
    ADD UNIQUE INDEX `account_UNIQUE` (`account`);
