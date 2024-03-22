ALTER TABLE `koin`.`versions`
CHANGE COLUMN `version` `version` VARCHAR(20) NOT NULL;

INSERT INTO `koin`.`versions` (`version`, `type`) VALUES ('semester_timestamp', 'timetable');
