CREATE TABLE IF NOT EXISTS `koin`.`club_category`
(
    `id`            INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `name`          VARCHAR(255) NOT NULL COMMENT '동아리 카테고리 이름',
    PRIMARY KEY (`id`)
);

INSERT INTO `koin`.`club_category` (`name`)
VALUES
    ('학술'),
    ('운동'),
    ('취미'),
    ('종교'),
    ('공연');
