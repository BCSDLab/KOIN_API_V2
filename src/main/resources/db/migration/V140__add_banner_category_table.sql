CREATE TABLE IF NOT EXISTS `koin`.`banner_categories`
(
    `id`            INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `name`          VARCHAR(255) NOT NULL COMMENT '배너 카테고리 이름',
    `description`   VARCHAR(255) NOT NULL COMMENT '배너 카테고리 설명',
    `created_at`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    PRIMARY KEY (`id`)
);

INSERT INTO `koin`.`banner_categories` (`name`, `description`)
VALUES ('메인 모달', '앱/웹 첫 화면에 뜨는 4:3 (1600x1200) 비율의 배너');
