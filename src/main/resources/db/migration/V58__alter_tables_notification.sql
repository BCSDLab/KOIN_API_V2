ALTER TABLE `koin`.`notification`
    ADD COLUMN `device_id` INT UNSIGNED NOT NULL;

ALTER TABLE `koin`.`notification`
DROP FOREIGN KEY `FK_NOTIFICATION_ON_USER FOREIGN KEY`;

ALTER TABLE `koin`.`notification`
    CHANGE COLUMN `users_id` `users_id` INT UNSIGNED NULL COMMENT '유저 id',
DROP INDEX `FK_NOTIFICATION_ON_USER FOREIGN KEY`;


ALTER TABLE `koin`.`notification_subscribe`
    ADD COLUMN `device_id` INT UNSIGNED NOT NULL;

ALTER TABLE `koin`.`notification_subscribe`
DROP FOREIGN KEY `FK_NOTIFICATION_SUBSCRIBE_ON_USER`;

ALTER TABLE `koin`.`notification_subscribe`
    CHANGE COLUMN `user_id` `user_id` INT UNSIGNED NULL COMMENT '유저 id',
DROP INDEX `unique_user_id_subscribe_type_detail_type`;
