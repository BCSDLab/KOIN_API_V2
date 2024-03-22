ALTER TABLE `koin`.`users`
DROP COLUMN `remember_token`,
DROP COLUMN `identity`,
ADD COLUMN `user_type` VARCHAR(20) NOT NULL AFTER `phone_number`;