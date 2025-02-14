-- 학과 테이블 생성
CREATE TABLE if not exists `koin`.`department`
(
    `id`       INT UNSIGNED NOT NULL AUTO_INCREMENT comment '고유 id',
    `name`     VARCHAR(255) NOT NULL comment '학과 이름',
    PRIMARY KEY (`id`),
    created_at timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP comment '생성 일자',
    updated_at timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '수정 일자'
);

INSERT INTO `department` (name)
VALUES ('컴퓨터공학부'),
       ('기계공학부'),
       ('메카트로닉스공학부'),
       ('전기전자통신공학부'),
       ('디자인공학부'),
       ('건축공학부'),
       ('화학생명공학부'),
       ('에너지신소재화학공학부'),
       ('산업경영학부'),
       ('고용서비스정책학과'),
       ('학과공통');
