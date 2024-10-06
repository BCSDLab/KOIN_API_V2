-- 대학요람 테이블 생성
CREATE TABLE `catalog`
(
    `id`             INT UNSIGNED NOT NULL AUTO_INCREMENT comment '고유 id',
    `year`           VARCHAR(20)  NOT NULL comment '년도',
    `code`           VARCHAR(20)  NOT NULL comment '강의 코드',
    `lecture_name`   VARCHAR(255) NOT NULL comment '강의 이름',
    `department_id`  INT UNSIGNED NOT NULL comment '학과 id',
    `credit`         INT UNSIGNED NOT NULL DEFAULT 0 comment '학점',
    `course_type_id` INT UNSIGNED NOT NULL comment '이수 구분 id',
    PRIMARY KEY (`id`),
    FOREIGN KEY (`course_type_id`) REFERENCES `course_type` (`id`),
    FOREIGN KEY (`department_id`) REFERENCES `department` (`id`),
    created_at       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP comment '생성 일자',
    updated_at       timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '수정 일자',
    key              catalog_year_idx(year)
);
