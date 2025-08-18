ALTER TABLE `koin`.`admins`
    ADD UNIQUE KEY `UK_admin_user_id` (`user_id`);

ALTER TABLE `koin`.`admins`
    DROP FOREIGN KEY `FK_ADMIN_ON_USER`;

ALTER TABLE `koin`.`admins`
    DROP PRIMARY KEY;

ALTER TABLE `koin`.`admins`
    ADD COLUMN `id` INT UNSIGNED NOT NULL COMMENT '어드민 고유 ID' AUTO_INCREMENT PRIMARY KEY FIRST;

ALTER TABLE `koin`.`admins`
    ADD CONSTRAINT `FK_ADMIN_ON_USER`
        FOREIGN KEY (`user_id`) REFERENCES `koin`.`users`(`id`) ON DELETE CASCADE;
