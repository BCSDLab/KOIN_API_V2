INSERT INTO `coop_semester` (`semester`, `from_date`, `to_date`, `is_applied`)
VALUES ('24-동계방학', '2024-12-23', '2025-03-02', 1);

SET @SEMESTER_ID = LAST_INSERT_ID();

UPDATE `coop_semester`
SET `is_applied` = 0
WHERE `id` = 1;

INSERT INTO `coop_shop` (`name`, `phone`, `location`, `remarks`, `semester_id`)
VALUES ('학생식당', '041-560-1278', '학생회관 2층', '비계절학기 주말 미운영', @SEMESTER_ID),
        ('복지관식당', '041-560-1778', '복지관 2층', '복지관식당 c코너 별도 운영(계절학기까지)', @SEMESTER_ID),
        ('대즐', '041-560-1779', '복지관 1층', '배달 가능', @SEMESTER_ID),
        ('서점', '041-560-1756', '복지관 1층', '점심시간 12:00 - 13:00', @SEMESTER_ID),
        ('세탁소', '041-552-1489', '학생회관 2층', NULL, @SEMESTER_ID),
        ('복사실', '041-560-1093', '학생회관 2층', '점심시간 11:30 - 12:30', @SEMESTER_ID),
        ('복지관 참빛관 편의점', '041-560-1093', '복지관 1층, 참빛관 1층', NULL, @SEMESTER_ID),
        ('미용실', '041-560-1769', '학생회관 1층', '예약제운영', @SEMESTER_ID),
        ('오락실', '041-560-1472', '학생회관 1층', NULL, @SEMESTER_ID);

SET @COOP_SHOP_ID = LAST_INSERT_ID();

INSERT INTO `coop_opens` (`coop_shop_id`, `type`, `day_of_week`, `open_time`, `close_time`)
VALUES (@COOP_SHOP_ID, '아침', 'WEEKDAYS', '08:00', '09:00'),
        (@COOP_SHOP_ID, '점심', 'WEEKDAYS', '11:30', '13:30'),
        (@COOP_SHOP_ID, '저녁', 'WEEKDAYS', '17:30', '18:30'),
        (@COOP_SHOP_ID, '아침', 'WEEKEND', '휴점(예약)', '휴점(예약)'),
        (@COOP_SHOP_ID, '점심', 'WEEKEND', '11:30', '13:30'),
        (@COOP_SHOP_ID, '저녁', 'WEEKEND', '17:30', '18:30'),
        (@COOP_SHOP_ID + 1, '점심', 'WEEKDAYS', '11:40', '13:30'),
        (@COOP_SHOP_ID + 1, '점심', 'WEEKEND', '미운영', '미운영'),
        (@COOP_SHOP_ID + 2, NULL, 'WEEKDAYS', '08:30', '19:00'),
        (@COOP_SHOP_ID + 2, NULL, 'WEEKDAYS', '휴점', '휴점'),
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
        (@COOP_SHOP_ID + 8, NULL, 'WEEKEND', '24시간', '24시간');