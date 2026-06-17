CREATE TABLE IF NOT EXISTS `koin`.`club_sns`
(
    `id`                 INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `club_id`            INT UNSIGNED NOT NULL COMMENT '동아리 고유 ID',
    `sns_type`           VARCHAR(50)  NOT NULL COMMENT '동아리 SNS 타입',
    `contact`            VARCHAR(255) NOT NULL COMMENT '동아리 SNS 연락처',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`club_id`) REFERENCES `koin`.`club` (`id`)
);
