CREATE TABLE IF NOT EXISTS `koin`.`club`
(
    `id`                 INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `club_category_id`   INT UNSIGNED NOT NULL COMMENT '동아리 카테고리 ID',
    `name`               VARCHAR(50) NOT NULL COMMENT '동아리 이름',
    `hits`               INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '조회수',
    `description`        VARCHAR(100) NOT NULL COMMENT '동아리 소개',
    `is_active`          TINYINT(1) NOT NULL DEFAULT 0 COMMENT '활성화 여부',
    `image_url`          VARCHAR(255) NOT NULL COMMENT '동아리 사진',
    `likes`              INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '동아리 좋아요 개수',
    `location`           VARCHAR(20) NOT NULL COMMENT '동아리 장소',
    `last_week_hits`     INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '지난 주 조회수',
    `introduction`       TEXT NOT NULL COMMENT '동아리 상세 소개',
    `created_at`         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`club_category_id`) REFERENCES `koin`.`club_category` (`id`)
);
