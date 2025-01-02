CREATE TABLE IF NOT EXISTS `koin`.`lecture_information`(
    `id`         int UNSIGNED NOT NULL AUTO_INCREMENT,
    `lecture_id` int UNSIGNED NOT NULL,
    `start_time` int NULL,
    `end_time`   int NULL,
    PRIMARY KEY (`id`),
    index (`lecture_id`)
);
