CREATE TABLE IF NOT EXISTS `koin`.`timetable_regular_lecture_information`
(
    `id`                     INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 id',
    `timetable_lecture_id`   INT UNSIGNED NOT NULL COMMENT 'timetable_lecture 고유 id',
    `lecture_information_id` INT UNSIGNED NOT NULL COMMENT 'lecture_information 고유 id',
    `place`                  VARCHAR(255) NOT NULL COMMENT '강의 장소',
    PRIMARY KEY (id),
    INDEX(timetable_lecture_id)
)
