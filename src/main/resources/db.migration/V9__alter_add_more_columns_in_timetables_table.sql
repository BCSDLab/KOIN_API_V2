ALTER TABLE `koin`.`timetables`
ADD COLUMN `code` VARCHAR(10) AFTER `semester_id`,
ADD COLUMN `lecture_class` VARCHAR(3) AFTER `grades`,
ADD COLUMN `target` VARCHAR(200) AFTER `lecture_class`,
ADD COLUMN `regular_number` VARCHAR(4) AFTER `target`,
ADD COLUMN `design_score` VARCHAR(4) AFTER `regular_number`,
ADD COLUMN `department` VARCHAR(30) AFTER `design_score`;