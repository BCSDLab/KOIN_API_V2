ALTER TABLE `koin`.`lectures`
    ADD CONSTRAINT `FK_LECTURES_ON_SEMESTER`
        FOREIGN KEY (`semester_id`)
            REFERENCES `koin`.`semester` (`id`)
            ON UPDATE CASCADE;
