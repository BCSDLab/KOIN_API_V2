CREATE TABLE `koin`.`semester` (
  `id` INT UNSIGNED NOT NULL,
  `semester` VARCHAR(10) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `semester_UNIQUE` (`semester` ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;
