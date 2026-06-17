CREATE TABLE IF NOT EXISTS `koin`.`club_event_image`
(
    `id`             INT UNSIGNED     NOT NULL AUTO_INCREMENT COMMENT '동아리 행사 이미지 고유 ID',
    `club_event_id`  INT UNSIGNED     NOT NULL COMMENT '동아리 행사 고유 ID',
    `image_url`      VARCHAR(255)     NOT NULL COMMENT '이미지 URL',
    `created_at`     TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at`     TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`club_event_id`) REFERENCES `koin`.`club_event` (`id`) ON DELETE CASCADE
);
