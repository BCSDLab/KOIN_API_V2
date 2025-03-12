INSERT INTO `coop_semester` (`semester`, `from_date`, `to_date`, `is_applied`)
VALUES ('25-1학기', '2025-03-04', '2025-06-20', 1);

SET @SEMESTER_ID = LAST_INSERT_ID();

INSERT INTO `coop_shop` (`coop_name_id`, `phone`, `location`, `remarks`, `semester_id`)
VALUES (1, '041-560-1278', '학생회관 2층', '천원의 아침밥 평일 운영', @SEMESTER_ID),
       (2, '041-560-1778', '복지관 2층', '능수관', @SEMESTER_ID),
       (3, '041-560-1779', '복지관 1층', '배달 서비스, 공·일요일 미운영', @SEMESTER_ID),
       (4, '041-560-1756', '복지관 1층', '점심시간 12:00 - 13:00', @SEMESTER_ID),
       (5, '041-552-1489', '학생회관 2층', NULL, @SEMESTER_ID),
       (6, '041-560-1093', '학생회관 2층', '점심시간 11:30 - 12:30', @SEMESTER_ID),
       ( 7, '041-560-1093', '복지관 1층, 참빛관 1층', '복지관 임시운영 별도공지', @SEMESTER_ID),
       (8, '041-560-1769', '학생회관 1층', '예약제운영', @SEMESTER_ID),
       (9, '041-560-1472', '학생회관 1층', NULL, @SEMESTER_ID),
       (10, '041-560-1758', '학생회관 1층', '점심시간 14:00 - 15:00', @SEMESTER_ID),
       (11, '041-560-1760', '학생회관 1층', '점심시간 12:00 - 13:00', @SEMESTER_ID);

SET @COOP_SHOP_ID = LAST_INSERT_ID();

INSERT INTO coop_opens (`coop_shop_id`, `type`, `day_of_week`, `open_time`, `close_time`)
VALUES (@COOP_SHOP_ID, '아침', 'WEEKDAYS', '08:00', '09:30'),
       (@COOP_SHOP_ID, '점심', 'WEEKDAYS', '11:30', '13:30'),
       (@COOP_SHOP_ID, '저녁', 'WEEKDAYS', '17:30', '18:30'),
       (@COOP_SHOP_ID, '아침', 'WEEKEND', '조식 미운영', '조식 미운영'),
       (@COOP_SHOP_ID, '점심', 'WEEKEND', '11:30', '13:30'),
       (@COOP_SHOP_ID, '저녁', 'WEEKEND', '17:30', '18:30'),
       (@COOP_SHOP_ID + 1, '점심', 'WEEKDAYS', '11:40', '13:30'),
       (@COOP_SHOP_ID + 1, '점심', 'WEEKEND', '미운영', '미운영'),
       (@COOP_SHOP_ID + 2, NULL, 'WEEKDAYS', '08:30', '21:00'),
       (@COOP_SHOP_ID + 2, NULL, 'SATURDAY', '11:00', '18:30'),
       (@COOP_SHOP_ID + 3, NULL, 'WEEKDAYS', '09:00', '18:00'),
       (@COOP_SHOP_ID + 3, NULL, 'WEEKEND', '휴점', '휴점'),
       (@COOP_SHOP_ID + 4, NULL, 'WEEKDAYS', '11:30', '18:00'),
       (@COOP_SHOP_ID + 4, NULL, 'WEEKEND', '휴점', '휴점'),
       (@COOP_SHOP_ID + 5, NULL, 'WEEKDAYS', '08:30', '18:00'),
       (@COOP_SHOP_ID + 5, NULL, 'WEEKEND', '휴점', '휴점'),
       (@COOP_SHOP_ID + 6, NULL, 'WEEKDAYS', '24시간', '24시간'),
       (@COOP_SHOP_ID + 6, NULL, 'WEEKEND', '24시간', '24시간'),
       (@COOP_SHOP_ID + 7, NULL, 'WEEKDAYS', '09:30', '17:00'),
       (@COOP_SHOP_ID + 7, NULL, 'WEEKEND', '휴점', '휴점'),
       (@COOP_SHOP_ID + 8, NULL, 'WEEKDAYS', '24시간', '24시간'),
       (@COOP_SHOP_ID + 8, NULL, 'WEEKEND', '24시간', '24시간'),
       (@COOP_SHOP_ID + 9, NULL, 'WEEKDAYS', '09:00', '18:00'),
       (@COOP_SHOP_ID + 9, NULL, 'WEEKEND', '휴점', '휴점'),
       (@COOP_SHOP_ID + 10, NULL, 'WEEKDAYS', '10:00', '19:00'),
       (@COOP_SHOP_ID + 10, NULL, 'WEEKEND', '휴점', '휴점');

UPDATE `coop_semester`
SET `is_applied` = 0
WHERE `semester` = '24-동계방학';
