ALTER TABLE `koin`.`admins`
ADD COLUMN `grant_event` TINYINT(1) NOT NULL DEFAULT '0' AFTER `grant_bcsdlab`;