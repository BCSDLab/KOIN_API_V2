ALTER TABLE `koin`.`admins`
ADD COLUMN `grant_bcsdlab` TINYINT(1) NOT NULL DEFAULT '0' AFTER `grant_survey`;