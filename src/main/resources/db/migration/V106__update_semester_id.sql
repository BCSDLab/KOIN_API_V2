UPDATE `koin`.`lectures` l
SET `semester_id` = (SELECT `id`
                     FROM `koin`.`semester` s
                     WHERE l.`semester_date` = s.`semester`);
