CREATE TABLE IF NOT EXISTS `koin`.`lecture_information`(
    `id`         int UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 id',
    `lecture_id` int UNSIGNED NOT NULL COMMENT 'lecture 고유 id',
    `start_time` int NULL COMMENT '정규 강의 시작 시간',
    `end_time`   int NULL COMMENT '정규 강의 마감 시간',
    PRIMARY KEY (`id`),
    index (`lecture_id`)
);
