CREATE TABLE if not exists `koin`.`general_education_area_course_type_map`
(
    `id`       INT UNSIGNED NOT NULL AUTO_INCREMENT comment '고유 id',
    `year` INT UNSIGNED NOT NULL comment '연도',
    `general_education_area_id` INT UNSIGNED NOT NULL comment '교양 영역 id',
    `course_type_id` INT UNSIGNED NOT NULL comment '이수 구분 id',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`general_education_area_id`) REFERENCES `general_education_area` (`id`),
    FOREIGN KEY (`course_type_id`) REFERENCES `course_type` (`id`)
);
