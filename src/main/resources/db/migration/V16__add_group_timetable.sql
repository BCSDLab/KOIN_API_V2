CREATE TABLE `group_timetable` (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL comment '고유 id',
    user_id INT UNSIGNED NOT NULL comment '유저 id',
    semester_id INT UNSIGNED NOT NULL comment '학기 id',
    name VARCHAR(255) NULL comment '시간표 이름',
    is_main TINYINT(1) NOT NULL DEFAULT 0 comment '메인 시간표 여부',
    created_at   timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at   timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    PRIMARY KEY (`id`)
);
