-- `general_education_area_id` 컬럼 추가
ALTER TABLE `timetable_lecture`
    ADD COLUMN `general_education_area_id` INT UNSIGNED DEFAULT NULL COMMENT '교양영역 id';

-- 외래 키 추가
ALTER TABLE `timetable_lecture`
    ADD CONSTRAINT `FK_GENERAL_EDUCATION_AREA_ON_TIMETABLE_LECTURE`
        FOREIGN KEY (`general_education_area_id`)
            REFERENCES `general_education_area` (`id`)
            ON DELETE CASCADE;
