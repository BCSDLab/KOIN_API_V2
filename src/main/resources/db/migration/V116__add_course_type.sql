-- 이수구분 테이블 생성
CREATE TABLE if not exists `koin`.`course_type`
(
    id         INT UNSIGNED PRIMARY KEY AUTO_INCREMENT comment '고유 id',
    name       VARCHAR(255) NOT NULL comment '이수 구분 이름',
    is_deleted tinyint               DEFAULT 0 comment '삭제 여부',
    created_at timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP comment '생성 일자',
    updated_at timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP comment '수정 일자'
);
