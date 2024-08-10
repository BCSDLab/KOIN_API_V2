ALTER TABLE `timetable_lecture`
    ADD COLUMN `standard_graduation_requirements_id` INT UNSIGNED DEFAULT NULL COMMENT '기준 졸업요건 id';

ALTER TABLE `timetable_lecture`
    ADD CONSTRAINT `FK_STANDARD_GRADUATION_REQUIREMENTS_ON_TIMETABLE_LECTURE`
        FOREIGN KEY (`standard_graduation_requirements_id`)
            REFERENCES `standard_graduation_requirements` (`id`)
            ON DELETE CASCADE;
