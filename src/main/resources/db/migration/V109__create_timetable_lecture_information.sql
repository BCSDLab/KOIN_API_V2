CREATE TABLE IF NOT EXISTS `koin`.`timetable_lecture_information` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 id',
    `start_time` INT UNSIGNED NULL COMMENT '커스텀 강의 시작 시간',
    `end_time` INT UNSIGNED NULL COMMENT '커스텀 강의 종료 시간',
    `place` VARCHAR(255) NULL COMMENT '강의 장소',
    `lecture_information_id` INT UNSIGNED NULL COMMENT 'lecture_information 고유 id',
    `timetable_lecture_id` INT UNSIGNED NOT NULL COMMENT 'timetable_lecture 고유 id',
    PRIMARY KEY (id),
    INDEX(timetable_lecture_id)
)
