-- 대학요람 테이블 생성
CREATE TABLE if not exists `koin`.`catalog`
(
    `id`             INT UNSIGNED NOT NULL AUTO_INCREMENT comment '고유 id',
    `code`           VARCHAR(20)  NOT NULL comment '강의 코드',
    `lecture_name`   VARCHAR(255) NOT NULL comment '강의 이름',
    `department_id`  INT UNSIGNED NOT NULL comment '학과 id',
    `major_id`  INT UNSIGNED NOT NULL comment '전공 id',
    `credit`         INT UNSIGNED NOT NULL DEFAULT 0 comment '학점',
    `course_type_id` INT UNSIGNED NOT NULL comment '이수 구분 id',
    `general_education_area_id` INT UNSIGNED NULL comment '교양 영역 id',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`major_id`) REFERENCES `major` (`id`),
    FOREIGN KEY (`course_type_id`) REFERENCES `course_type` (`id`),
    FOREIGN KEY (`department_id`) REFERENCES `department` (`id`),
    FOREIGN KEY (`general_education_area_id`) REFERENCES `general_education_area` (`id`),
    created_at       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP comment '생성 일자',
    updated_at       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '수정 일자'
);
