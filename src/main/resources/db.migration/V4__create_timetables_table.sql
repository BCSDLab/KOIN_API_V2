CREATE TABLE `koin`.`timetables` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` INT UNSIGNED NOT NULL,
  `semester_id` INT UNSIGNED NOT NULL,
  `class_title` VARCHAR(50) NOT NULL,
  `class_time` VARCHAR(100) NOT NULL,
  `class_place` VARCHAR(30) NULL,
  `professor` VARCHAR(30) NULL,
  `memo` VARCHAR(200) NULL,
  PRIMARY KEY (`id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;