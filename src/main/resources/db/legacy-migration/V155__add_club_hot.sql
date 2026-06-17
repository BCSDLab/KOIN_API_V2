CREATE TABLE IF NOT EXISTS `koin`.`club_hot`
(
    `id`                 INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `club_id`            INT UNSIGNED NOT NULL COMMENT '인기 동아리 고유 ID',
    `ranking`               INT UNSIGNED NOT NULL COMMENT '순위',
    `period_hits`        INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '기간 조회수',
    `start_date`         DATE NOT NULL COMMENT '집계 시작일',
    `end_date`           DATE NOT NULL COMMENT '집계 종료일',
    `created_at`         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`club_id`) REFERENCES `koin`.`club` (`id`)
);
