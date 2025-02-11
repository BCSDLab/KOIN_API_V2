-- 졸업학점 계산 감지 테이블
CREATE TABLE if not exists `koin`.`detect_graduation_calculation`
(
    id INT UNSIGNED PRIMARY KEY AUTO_INCREMENT comment '고유 id',
    user_id INT UNSIGNED NULL comment '유저 id',
    is_changed TINYINT(1) NULL DEFAULT 0 comment '졸업학점 계산 변경 여부'
);
