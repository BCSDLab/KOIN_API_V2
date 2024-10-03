CREATE TABLE `koin`.`new_articles`
(
    `id`         INT UNSIGNED                         NOT NULL AUTO_INCREMENT COMMENT 'notice articles 고유 id',
    `board_id`   INT UNSIGNED                         NOT NULL COMMENT '게시판 고유 id',
    `title`      VARCHAR(255) CHARACTER SET 'utf8mb4' NOT NULL COMMENT '제목',
    `content`    MEDIUMTEXT CHARACTER SET 'utf8mb4'   NULL     DEFAULT NULL COMMENT '내용',
    `hit`        INT UNSIGNED                         NOT NULL DEFAULT '0' COMMENT '조회수',
    `is_notice`  TINYINT(1)                           NOT NULL DEFAULT '0' COMMENT '공지사항인지 여부',
    `is_deleted` TINYINT(1)                           NOT NULL DEFAULT '0' COMMENT '삭제 여부',
    `created_at` TIMESTAMP                            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at` TIMESTAMP                            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
    PRIMARY KEY (`id`)
);

CREATE TABLE `koin`.`new_koin_articles`
(
    `id`         INT          NOT NULL AUTO_INCREMENT,
    `article_id` INT UNSIGNED NOT NULL,
    `user_id`    INT UNSIGNED NULL,
    `is_deleted` TINYINT(1)   NOT NULL DEFAULT '0' COMMENT '삭제 여부',
    `created_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
    PRIMARY KEY (`id`)
);

CREATE TABLE `koin`.`new_koreatech_articles`
(
    `id`            INT UNSIGNED                         NOT NULL AUTO_INCREMENT COMMENT 'notice articles 고유 id',
    `article_id`    INT UNSIGNED                         NOT NULL,
    `author`        VARCHAR(50) CHARACTER SET 'utf8mb4'  NOT NULL COMMENT '작성자',
    `portal_num`    INT UNSIGNED                         NOT NULL COMMENT '게시물 번호',
    `portal_hit`    INT UNSIGNED                         NOT NULL DEFAULT '0',
    `url`           VARCHAR(255) CHARACTER SET 'utf8mb4' NOT NULL COMMENT '기존 게시글 url',
    `registered_at` DATETIME                             NULL     DEFAULT NULL COMMENT '등록 일자',
    `is_deleted`    TINYINT(1)                           NOT NULL DEFAULT '0' COMMENT '삭제 여부',
    `created_at`    TIMESTAMP                            NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`    TIMESTAMP                            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
    PRIMARY KEY (`id`)
);
