CREATE TABLE `timetable_lecture` (
    id INT UNSIGNED AUTO_INCREMENT NOT NULL comment '고유 id' primary key,
    class_title VARCHAR(255) NULL comment '수업 이름',
    class_time VARCHAR(255) NULL comment '강의 시간',
    class_place VARCHAR(255) NULL comment '수업 장소',
    professor VARCHAR(255) NULL comment '교수',
    grades VARCHAR(2) not null comment '학점' default '0',
    memo VARCHAR(255) NULL comment '메모',
    is_deleted TINYINT(1) NULL DEFAULT 0 comment '삭제 여부',
    created_at   timestamp  default CURRENT_TIMESTAMP not null comment '생성 일자',
    updated_at   timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '업데이트 일자',
    lectures_id INT UNSIGNED NULL comment '강의_id',
    timetable_id INT UNSIGNED NULL comment '그룹시간표_id',
    user_id INT UNSIGNED NULL comment '유저 id',
    semester_id INT UNSIGNED NULL comment '학기 id',
    CONSTRAINT FK_TIMETABLE_FRAME_ON_TIMETABLE_LECTURE FOREIGN KEY (timetable_id) references `koin`.`timetable_frame`(id) on delete cascade
);
