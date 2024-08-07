CREATE TABLE timetable_frame (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL comment '고유 id',
    user_id INT UNSIGNED NOT NULL comment '유저 id',
    semester_id INT UNSIGNED NOT NULL comment '학기 id',
    name VARCHAR(255) NOT NULL comment '시간표 이름',
    is_main TINYINT(1) NOT NULL DEFAULT 0 comment '메인 시간표 여부',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 comment '시간표 삭제 여부',
    created_at   timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at   timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '수정 일자',
    PRIMARY KEY (`id`),
    INDEX timetable_frame_INDEX (user_id, semester_id) USING BTREE
);
