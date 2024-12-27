UPDATE coop_opens AS co
    JOIN coop_shop AS cs ON co.coop_shop_id = cs.id
    SET co.day_of_week = 'WEEKEND'
WHERE cs.name = '대즐';

INSERT INTO `coop_shop` (`name`, `phone`, `location`, `remarks`, `semester_id`)
VALUES ('우편취급국', '041-560-1758', '학생회관 1층', '점심시간 12:00-13:00', 2),
       ('안경원', '041-560-1760', '복지관 1층', '점심시간 13:00-14:00', 2);

SET @COOP_SHOP_ID = LAST_INSERT_ID();

INSERT INTO `coop_opens` (`coop_shop_id`, `type`, `day_of_week`, `open_time`, `close_time`)
VALUES (@COOP_SHOP_ID, NULL, 'WEEKDAYS', '09:00', '18:00'),
       (@COOP_SHOP_ID, NULL, 'WEEKEND', '휴점', '휴점'),
       (@COOP_SHOP_ID + 1, NULL, 'WEEKDAYS', '11:00', '17:00'),
       (@COOP_SHOP_ID + 1, NULL, 'WEEKEND', '휴점', '휴점'),
       (@COOP_SHOP_ID + 1, NULL, 'FRIDAY', '휴점', '휴점');

