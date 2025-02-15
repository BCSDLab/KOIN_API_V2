-- `course_type_id` 컬럼 추가
ALTER TABLE `timetable_lecture`
    ADD COLUMN `course_type_id` INT UNSIGNED DEFAULT NULL COMMENT '이수구분 id';

-- 외래 키 추가
ALTER TABLE `timetable_lecture`
    ADD CONSTRAINT `FK_COURSE_TYPE_ON_TIMETABLE_LECTURE`
        FOREIGN KEY (`course_type_id`)
            REFERENCES `course_type` (`id`)
            ON DELETE CASCADE;
