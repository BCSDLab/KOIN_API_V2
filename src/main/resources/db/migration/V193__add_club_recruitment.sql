CREATE TABLE IF NOT EXISTS `koin`.`club_recruitment`
(
    `id`                    INT UNSIGNED        NOT NULL AUTO_INCREMENT COMMENT '동아리 모집 고유 ID',
    `club_id`               INT UNSIGNED        NOT NULL COMMENT '동아리 고유 ID',
    `start_date`            DATE                NULL COMMENT '동아리 모집 시작 날짜',
    `end_date`              DATE                NULL COMMENT '동아리 모집 마감 날짜',
    `is_always_recruiting`  TINYINT(1)          NOT NULL COMMENT '동아리 상시 모집 여부',
    `image_url`             VARCHAR(255)        NULL COMMENT '동아리 모집 이미지'
    `content`               TEXT                NOT NULL COMMENT '동아리 모집 내용',
    `created_at`            TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at`            TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_club_recruitment_club_id` (`club_id`),
    CONSTRAINT `fk_club` FOREIGN KEY (`club_id`) REFERENCES `koin`.`club` (`id`)
);
