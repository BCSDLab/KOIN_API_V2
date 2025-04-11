ALTER TABLE `koin`.`admins`
    DROP COLUMN `can_create_admin`;

ALTER TABLE `koin`.`admins`
    DROP COLUMN `super_admin`;

ALTER TABLE `koin`.`admins`
    ADD COLUMN `name` VARCHAR(10) NULL COMMENT '이름';

ALTER TABLE `koin`.`admins`
    ADD COLUMN `email` VARCHAR(100) NULL COMMENT '이메일';

ALTER TABLE `koin`.`admins`
    ADD COLUMN  `phone_number` VARCHAR(20) NULL COMMENT '전화번호';

ALTER TABLE `koin`.`admins`
    ADD COLUMN  `role` VARCHAR(20) NULL COMMENT '직위';
