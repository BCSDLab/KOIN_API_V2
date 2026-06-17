-- 학생 계산 테이블 생성
CREATE TABLE if not exists `koin`.`student_course_calculation`
(
    id                                  INT UNSIGNED PRIMARY KEY AUTO_INCREMENT comment '고유 id',
    user_id                             INT UNSIGNED NOT NULL comment '유저 id',
    standard_graduation_requirements_id INT UNSIGNED comment '기준 졸업 요건 id',
    completed_grades                    INT       NOT NULL DEFAULT 0 comment '이수 학점',
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (standard_graduation_requirements_id) REFERENCES
        standard_graduation_requirements (id),
    is_deleted                          tinyint            DEFAULT 0 comment '삭제 여부',
    created_at                          timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP comment '생성 일자',
    updated_at                          timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '수정 일자'
);
