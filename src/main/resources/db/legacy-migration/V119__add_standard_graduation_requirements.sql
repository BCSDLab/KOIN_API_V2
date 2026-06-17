CREATE TABLE if not exists `koin`.`standard_graduation_requirements`
(
    id              INT UNSIGNED PRIMARY KEY AUTO_INCREMENT comment '고유 id',
    year            VARCHAR(20) NOT NULL comment '년도',
    major_id        INT UNSIGNED NOT NULL comment '전공 id',
    course_type_id  INT UNSIGNED comment '이수 구분 id',
    required_grades INT         NOT NULL comment '기준 학점',
    FOREIGN KEY (course_type_id) REFERENCES course_type (id),
    FOREIGN KEY (major_id) REFERENCES major (id),
    is_deleted      tinyint              DEFAULT 0 comment '삭제 여부',
    created_at      timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP comment '생성 일자',
    updated_at      timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '수정 일자'
);
