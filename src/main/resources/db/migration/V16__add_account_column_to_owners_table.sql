ALTER TABLE `koin`.`owners`
    ADD COLUMN `account` VARCHAR(255) NULL COMMENT '사장님 전화번호(“-“ 제거, unique)',
    ADD UNIQUE INDEX `account_UNIQUE` (`account`);

UPDATE `koin`.`owners` o
    JOIN `koin`.`users` u ON o.user_id = u.id
    SET o.account = REPLACE(u.phone_number, '-', '')
