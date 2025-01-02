CREATE TABLE IF NOT EXISTS `koin`.`timetable_custom_lecture_information` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 id',
    `timetable_lecture_id` INT UNSIGNED NOT NULL COMMENT 'timetableLecture id',
    `start_time` INT UNSIGNED NOT NULL COMMENT '시작 시간',
    `end_time` INT UNSIGNED NOT NULL COMMENT '종료 시간',
    `place` VARCHAR(255) NULL COMMENT '장소',
    PRIMARY KEY (id),
    INDEX(timetable_lecture_id)
)
