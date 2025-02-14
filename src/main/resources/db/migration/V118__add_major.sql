CREATE TABLE if not exists `koin`.`major`
(
    `id`       INT UNSIGNED NOT NULL AUTO_INCREMENT comment '고유 id',
    `name`     VARCHAR(255) NOT NULL comment '전공 이름',
    `department_id`  INT UNSIGNED NOT NULL comment '학과 id',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`department_id`) REFERENCES `department` (`id`),
    created_at timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP comment '생성 일자',
    updated_at timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '수정 일자'
    );

INSERT INTO `major` (name, department_id)
VALUES ('컴퓨터공학전공', 1),
       ('친환경자동차･에너지트랙전공', 2),
       ('시스템설계제조전공', 2),
       ('스마트모빌리티전공', 2),
       ('생산시스템전공', 3),
       ('제어시스템전공', 3),
       ('디지털시스템전공', 3),
       ('전기공학전공', 4),
       ('전자공학전공', 4),
       ('정보통신공학전공', 4),
       ('디자인공학전공', 5),
       ('건축공학전공', 6),
       ('화학생명공학전공', 7),
       ('에너지신소재공학전공', 8),
       ('융합경영전공', 9),
       ('데이터경영전공', 9)