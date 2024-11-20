ALTER TABLE `koin`.`lectures`
    ADD COLUMN `semester_id` INT UNSIGNED NOT NULL;

UPDATE `koin`.`lectures` l
SET `semester_id` = (SELECT `id`
                     FROM `koin`.`semester` s
                     WHERE l.`semester_date` = s.`semester`);

ALTER TABLE `koin`.`lectures`
    ADD CONSTRAINT `FK_LECTURES_ON_SEMESTER`
        FOREIGN KEY (`semester_id`)
            REFERENCES `koin`.`semester` (`id`)
            ON DELETE CASCADE;
