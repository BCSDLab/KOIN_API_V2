ALTER TABLE `koin`.`users`
ADD COLUMN `email` VARCHAR(100) NOT NULL COMMENT '학교 email' AFTER `user_type` ;