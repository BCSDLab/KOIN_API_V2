ALTER TABLE `timetable_lecture`
    ADD COLUMN `course_type_id` INT UNSIGNED DEFAULT NULL COMMENT '이수 구분 id';

ALTER TABLE `timetable_lecture`
    ADD CONSTRAINT `FK_COURSE_TYPE_ON_TIMETABLE_LECTURE`
        FOREIGN KEY (`course_type_id`)
            REFERENCES `course_type` (`id`)
            ON DELETE CASCADE;
