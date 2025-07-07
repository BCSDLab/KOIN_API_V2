CREATE TABLE IF NOT EXISTS `koin`.`rider_message`
(
    `id`              INT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '배달 기사 요청 사항 고유 ID',
    `content`         VARCHAR(100)   NOT NULL COMMENT '배달 기사 요청 사항',
    `created_at`      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    PRIMARY KEY (`id`)
);

INSERT INTO `koin`.`rider_message` (`content`)
VALUES ('문 앞에 놔주세요 (벨 눌러주세요)'),
       ('문 앞에 놔주세요 (노크해주세요)'),
       ('문 앞에 놔주세요 (벨X, 노크 X)'),
       ('직접 받을게요'),
       ('전화주시면 마중 나갈게요');

