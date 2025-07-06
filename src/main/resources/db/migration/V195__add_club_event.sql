CREATE TABLE IF NOT EXISTS `koin`.`club_event`
(
    `id`                       INT UNSIGNED        NOT NULL AUTO_INCREMENT COMMENT '동아리 행사 고유 ID',
    `club_id`                  INT UNSIGNED        NOT NULL COMMENT '동아리 고유 ID',
    `name`                     VARCHAR(30)         NOT NULL COMMENT '동아리 행사 이름',
    `start_date`               DATETIME            NOT NULL COMMENT '동아리 행사 시작 날짜',
    `end_date`                 DATETIME            NOT NULL COMMENT '동아리 행사 종료 날짜',
    `introduce`                VARCHAR(70)         NOT NULL COMMENT '동아리 행사 간단 소개',
    `content`                  TEXT                NULL COMMENT '동아리 행사 상세 설명',
    `created_at`               TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at`               TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_club_event_club` FOREIGN KEY (`club_id`) REFERENCES `koin`.`club` (`id`) ON DELETE CASCADE
);
