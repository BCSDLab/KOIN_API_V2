ALTER TABLE coop_semester AUTO_INCREMENT = 1;
ALTER TABLE coop_shop AUTO_INCREMENT = 2;
ALTER TABLE coop_opens AUTO_INCREMENT = 7;

insert into `coop_semester` (`semester`, `from_date`, `to_date`, `is_applied`)
values ('24-2학기', '2024-09-02', '2024-12-20', 1);

INSERT INTO `coop_shop` (`name`, `phone`, `location`, `remarks`, `semester_id`)
VALUES ('복지관식당', '041-560-1778', '복지관 2층', '능수관', 1),
       ('대즐', '041-560-1779', '복지관 1층', '배달 서비스, 공·일요일 미운영', 1),
       ('서점', '041-560-1756', '복지관 1층', '점심시간 12:00 - 13:00', 1),
       ('세탁소', '041-552-1489', '학생회관 2층', NULL, 1),
       ('복사실', '041-560-1093', '학생회관 2층', '점심시간 11:30 - 12:30', 1),
       ('복지관 참빛관 편의점', '041-560-1093', '복지관 1층, 참빛관 1층', '복지관 임시운영 별도공지', 1),
       ('미용실', '041-560-1769', '학생회관 1층', '예약제운영', 1),
       ('오락실', '041-560-1472', '학생회관 1층', NULL, 1);

SET @FIRST_ID = LAST_INSERT_ID();

INSERT INTO coop_opens (`coop_shop_id`, `type`, `day_of_week`, `open_time`, `close_time`)
VALUES (@FIRST_ID, '점심', 'WEEKDAYS', '11:40', '13:30'),
       (@FIRST_ID, '점심', 'WEEKEND', '미운영', '미운영'),
       (@FIRST_ID + 1, NULL, 'WEEKDAYS', '08:30', '21:00'),
       (@FIRST_ID + 1, NULL, 'SATURDAY', '11:00', '18:00'),
       (@FIRST_ID + 2, NULL, 'WEEKDAYS', '09:00', '18:00'),
       (@FIRST_ID + 2, NULL, 'WEEKEND', '휴점', '휴점'),
       (@FIRST_ID + 3, NULL, 'WEEKDAYS', '11:30', '18:00'),
       (@FIRST_ID + 3, NULL, 'WEEKEND', '휴점', '휴점'),
       (@FIRST_ID + 4, NULL, 'WEEKDAYS', '08:30', '18:00'),
       (@FIRST_ID + 4, NULL, 'WEEKEND', '휴점', '휴점'),
       (@FIRST_ID + 5, NULL, 'WEEKDAYS', '24시간', '24시간'),
       (@FIRST_ID + 5, NULL, 'WEEKEND', '24시간', '24시간'),
       (@FIRST_ID + 6, NULL, 'WEEKDAYS', '09:30', '17:00'),
       (@FIRST_ID + 6, NULL, 'WEEKEND', '휴점', '휴점'),
       (@FIRST_ID + 7, NULL, 'WEEKDAYS', '24시간', '24시간'),
       (@FIRST_ID + 7, NULL, 'WEEKEND', '24시간', '24시간');

UPDATE coop_shop
SET semester_id = 1
WHERE name = '학생식당';

UPDATE coop_opens
SET day_of_week = 'WEEKDAYS'
WHERE coop_shop_id = 1
  AND day_of_week = '평일';

UPDATE coop_opens
SET day_of_week = 'WEEKEND'
WHERE coop_shop_id = 1
  AND day_of_week = '주말';
