ALTER TABLE `koin`.`timetables`
ADD COLUMN `is_deleted` TINYINT(1) NOT NULL DEFAULT 0 AFTER `memo`;