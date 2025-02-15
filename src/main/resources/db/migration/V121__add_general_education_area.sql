-- 교양 영역 테이블
CREATE TABLE IF NOT EXISTS `koin`.`general_education_area`
(
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 id',
    `name` VARCHAR(20) NOT NULL COMMENT '교양 영역 이름',
    PRIMARY KEY(`id`)
)
