insert into `coop_shop_semester` (`semester`, `from_date`, `to_date`, `is_applied`)
values ('24-2학기', '2024-09-02', '2024-12-20', 1);

INSERT INTO `coop_shop` (`name`, `phone`, `location`, `remarks`, `semester_id`)
    VALUES
    ('복지관식당', '041-560-1778', 'NULL', '능수관', 1),
    ('대즐', '041-560-1779', '복지관 1층', '배달 서비스, 공·일요일 미운영', 1),
    ('서점', '041-560-1756', '복지관 1층', '점심시간 12:00 - 13:00', 1),
    ('세탁소', '041-552-1489', '학생회관 2층', 'NULL', 1),
    ('복사실', '041-560-1093', '학생회관 2층', '점심시간 11:30 - 12:30', 1),
    ('복지관 참빛관 편의점', '041-560-1093', '복지관 1층, 참빛관 1층', '복지관 임시운영 별도공지', 1),
    ('미용실', '041-560-1769', '학생회관 1층', '예약제운영', 1),
    ('오락실', '041-560-1472', '학생회관 1층', 'NULL', 1);

update `coop_opens` set `day_of_week` = 'WEEKDAYS' where `id` = 1;
update `coop_opens` set `day_of_week` = 'WEEKDAYS' where `id` = 2;
update `coop_opens` set `day_of_week` = 'WEEKDAYS' where `id` = 3;
update `coop_opens` set `day_of_week` = 'WEEKEND' where `id` = 4;
update `coop_opens` set `day_of_week` = 'WEEKEND' where `id` = 5;
update `coop_opens` set `day_of_week` = 'WEEKEND' where `id` = 6;

INSERT INTO coop_opens (coop_shop_id, type, day_of_week, open_time, close_time)
    VALUES
    (2, '점심', 'WEEKDAYS', '11:40', '13:30'),
    (2, '점심', 'WEEKEND', '미운영', '미운영'),
    (3, 'NULL', 'WEEKDAYS', '08:30', '21:00'),
    (3, 'NULL', 'SATURDAY', '11:00', '18:00'),
    (4, 'NULL', 'WEEKDAYS', '09:00', '18:00'),
    (4, 'NULL', 'WEEKEND', '휴점', '휴점'),
    (5, 'NULL', 'WEEKDAYS', '11:30', '18:00'),
    (5, 'NULL', 'WEEKEND', '휴점', '휴점'),
    (6, 'NULL', 'WEEKDAYS', '08:30', '18:00'),
    (6, 'NULL', 'WEEKEND', '휴점', '휴점'),
    (7, 'NULL', 'WEEKDAYS', '24시간', '24시간'),
    (7, 'NULL', 'WEEKEND', '24시간', '24시간'),
    (8, 'NULL', 'WEEKDAYS', '09:30', '17:00'),
    (8, 'NULL', 'WEEKEND', '휴점', '휴점'),
    (9, 'NULL', 'WEEKDAYS', '24시간', '24시간'),
    (9, 'NULL', 'WEEKEND', '24시간', '24시간');
