INSERT INTO `koin`.`coop_semester` (`semester`, `from_date`, `to_date`, `is_applied`)
VALUES ('25-하계방학-비계절', '2025-07-12', '2025-08-31', 1);

SET @SEMESTER_ID = LAST_INSERT_ID();

INSERT INTO `koin`.`coop_shop` (`coop_name_id`, `phone`, `location`, `remarks`, `semester_id`)
VALUES (1, '041-560-1278', '학생회관 2층', '비계절학기 토/일 미운영', @SEMESTER_ID),
       (2, '041-560-1778', '복지관 2층', '능수관, 계절학기까지 운영', @SEMESTER_ID),
       (3, '041-560-1779', '복지관 1층', NULL, @SEMESTER_ID),
       (4, '041-560-1756', '복지관 1층', NULL, @SEMESTER_ID),
       (5, '041-552-1763', '학생회관 2층', '점심시간 12:00 - 13:00', @SEMESTER_ID),
       (6, '041-560-1093', '학생회관 2층', '점심시간 11:30 - 12:30', @SEMESTER_ID),
       (7, '041-560-1279', '복지관 1층, 참빛관 1층', NULL, @SEMESTER_ID),
       (8, '041-560-1372', '학생회관 1층', NULL, @SEMESTER_ID),
       (9, '041-560-1472', '학생회관 1층', NULL, @SEMESTER_ID),
       (10, '041-560-1758', '학생회관 1층', '점심시간 12:00 - 13:00', @SEMESTER_ID),
       (11, '041-560-1760', '학생회관 1층', '점심시간 13:00 - 14:00', @SEMESTER_ID);

SET @COOP_SHOP_ID = LAST_INSERT_ID();

INSERT INTO `koin`.`coop_opens` (`coop_shop_id`, `type`, `day_of_week`, `open_time`, `close_time`)
VALUES (@COOP_SHOP_ID, '아침', 'WEEKDAYS', '08:00', '09:00'),
       (@COOP_SHOP_ID, '점심', 'WEEKDAYS', '11:30', '13:30'),
       (@COOP_SHOP_ID, '저녁', 'WEEKDAYS', '17:30', '18:30'),
       (@COOP_SHOP_ID, '아침', 'WEEKEND', '미운영', '미운영'),
       (@COOP_SHOP_ID, '점심', 'WEEKEND', '미운영', '미운영'),
       (@COOP_SHOP_ID, '저녁', 'WEEKEND', '미운영', '미운영'),
       (@COOP_SHOP_ID + 1, '점심', 'WEEKDAYS', '미운영', '미운영'),
       (@COOP_SHOP_ID + 1, '점심', 'WEEKEND', '미운영', '미운영'),
       (@COOP_SHOP_ID + 2, NULL, 'WEEKDAYS', '08:30', '20:00'),
       (@COOP_SHOP_ID + 2, NULL, 'WEEKEND', '휴점', '휴점'),
       (@COOP_SHOP_ID + 3, NULL, 'WEEKDAYS', '09:00', '18:00'),
       (@COOP_SHOP_ID + 3, NULL, 'WEEKEND', '휴점', '휴점'),
       (@COOP_SHOP_ID + 4, NULL, 'WEEKDAYS', '10:30', '16:00'),
       (@COOP_SHOP_ID + 4, NULL, 'WEEKEND', '휴점', '휴점'),
       (@COOP_SHOP_ID + 5, NULL, 'WEEKDAYS', '10:00', '16:00'),
       (@COOP_SHOP_ID + 5, NULL, 'WEEKEND', '휴점', '휴점'),
       (@COOP_SHOP_ID + 6, NULL, 'WEEKDAYS', '24시간', '24시간'),
       (@COOP_SHOP_ID + 6, NULL, 'WEEKEND', '24시간', '24시간'),
       (@COOP_SHOP_ID + 7, NULL, 'WEEKDAYS', '10:00', '16:00'),
       (@COOP_SHOP_ID + 7, NULL, 'WEEKEND', '휴점', '휴점'),
       (@COOP_SHOP_ID + 8, NULL, 'WEEKDAYS', '24시간', '24시간'),
       (@COOP_SHOP_ID + 8, NULL, 'WEEKEND', '24시간', '24시간'),
       (@COOP_SHOP_ID + 9, NULL, 'WEEKDAYS', '09:00', '18:00'),
       (@COOP_SHOP_ID + 9, NULL, 'WEEKEND', '휴점', '휴점'),
       (@COOP_SHOP_ID + 10, NULL, 'WEEKDAYS', '11:00', '17:00'),
       (@COOP_SHOP_ID + 10, NULL, 'FRIDAY', '휴점', '휴점'),
       (@COOP_SHOP_ID + 10, NULL, 'WEEKEND', '휴점', '휴점');

UPDATE `koin`.`coop_semester`
SET `is_applied` = 0
WHERE `semester` = '25-하계방학';
