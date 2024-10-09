ALTER TABLE coop_semester AUTO_INCREMENT = 1;
ALTER TABLE coop_shop AUTO_INCREMENT = 2;
ALTER TABLE coop_opens AUTO_INCREMENT = 7;

insert into `coop_semester` (`semester`, `from_date`, `to_date`, `is_applied`)
values ('24-2학기', '2024-09-02', '2024-12-20', 1);

INSERT INTO `coop_shop` (`name`, `phone`, `location`, `remarks`, `semester_id`)
VALUES ('복지관식당', '041-560-1778', '복지관 2층', '능수관', 1);
INSERT INTO coop_opens (`coop_shop_id`, `type`, `day_of_week`, `open_time`, `close_time`)
VALUES (last_insert_id(), '점심', 'WEEKDAYS', '11:40', '13:30'),
       (last_insert_id(), '점심', 'WEEKEND', '미운영', '미운영');

INSERT INTO `coop_shop` (`name`, `phone`, `location`, `remarks`, `semester_id`)
VALUES ('대즐', '041-560-1779', '복지관 1층', '배달 서비스, 공·일요일 미운영', 1);
INSERT INTO coop_opens (`coop_shop_id`, `type`, `day_of_week`, `open_time`, `close_time`)
VALUES (last_insert_id(), NULL, 'WEEKDAYS', '08:30', '21:00'),
       (last_insert_id(), NULL, 'SATURDAY', '11:00', '18:00');

INSERT INTO `coop_shop` (`name`, `phone`, `location`, `remarks`, `semester_id`)
VALUES ('서점', '041-560-1756', '복지관 1층', '점심시간 12:00 - 13:00', 1);
INSERT INTO coop_opens (`coop_shop_id`, `type`, `day_of_week`, `open_time`, `close_time`)
VALUES (last_insert_id(), NULL, 'WEEKDAYS', '09:00', '18:00'),
       (last_insert_id(), NULL, 'WEEKEND', '휴점', '휴점');

INSERT INTO `coop_shop` (`name`, `phone`, `location`, `remarks`, `semester_id`)
VALUES ('세탁소', '041-552-1489', '학생회관 2층', NULL, 1);
INSERT INTO coop_opens (`coop_shop_id`, `type`, `day_of_week`, `open_time`, `close_time`)
VALUES (last_insert_id(), NULL, 'WEEKDAYS', '11:30', '18:00'),
       (last_insert_id(), NULL, 'WEEKEND', '휴점', '휴점');

INSERT INTO `coop_shop` (`name`, `phone`, `location`, `remarks`, `semester_id`)
VALUES ('복사실', '041-560-1093', '학생회관 2층', '점심시간 11:30 - 12:30', 1);
INSERT INTO coop_opens (`coop_shop_id`, `type`, `day_of_week`, `open_time`, `close_time`)
VALUES (last_insert_id(), NULL, 'WEEKDAYS', '08:30', '18:00'),
       (last_insert_id(), NULL, 'WEEKEND', '휴점', '휴점');

INSERT INTO `coop_shop` (`name`, `phone`, `location`, `remarks`, `semester_id`)
VALUES ('복지관 참빛관 편의점', '041-560-1093', '복지관 1층, 참빛관 1층', '복지관 임시운영 별도공지', 1);
INSERT INTO coop_opens (`coop_shop_id`, `type`, `day_of_week`, `open_time`, `close_time`)
VALUES (last_insert_id(), NULL, 'WEEKDAYS', '24시간', '24시간'),
       (last_insert_id(), NULL, 'WEEKEND', '24시간', '24시간');

INSERT INTO `coop_shop` (`name`, `phone`, `location`, `remarks`, `semester_id`)
VALUES ('미용실', '041-560-1769', '학생회관 1층', '예약제운영', 1);
INSERT INTO coop_opens (`coop_shop_id`, `type`, `day_of_week`, `open_time`, `close_time`)
VALUES (last_insert_id(), NULL, 'WEEKDAYS', '09:30', '17:00'),
       (last_insert_id(), NULL, 'WEEKEND', '휴점', '휴점');

INSERT INTO `coop_shop` (`name`, `phone`, `location`, `remarks`, `semester_id`)
VALUES ('오락실', '041-560-1472', '학생회관 1층', NULL, 1);
INSERT INTO coop_opens (`coop_shop_id`, `type`, `day_of_week`, `open_time`, `close_time`)
VALUES (last_insert_id(), 'NULL', 'WEEKDAYS', '24시간', '24시간'),
       (last_insert_id(), 'NULL', 'WEEKEND', '24시간', '24시간');

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
