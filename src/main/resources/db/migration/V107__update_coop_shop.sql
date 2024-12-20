UPDATE `coop_shop` SET `remarks` = '비계절학기 주말 미운영' WHERE `id` = 1;
UPDATE `coop_shop` SET `remarks` = '복지관식당 c코너 별도 운영(계절학기까지)' WHERE `id` = 2;
UPDATE `coop_shop` SET `remarks` = '배달 가능' WHERE `id` = 3;
UPDATE `coop_shop` SET `remarks` = NULL WHERE `id` = 7;

UPDATE `coop_opens` SET `close_time` = '19:00' WHERE `id` = 9;
UPDATE `coop_opens` SET `day_of_week` = 'WEEKDAYS', `open_time` = '휴점', `close_time` = '휴점' WHERE `id` = 10;
UPDATE `coop_opens` SET `open_time` = '10:30', `close_time` = '16:00' WHERE `id` = 13;
UPDATE `coop_opens` SET `open_time` = '10:00', `close_time` = '16:00' WHERE `id` = 19;
UPDATE `coop_opens` SET `open_time` = '10:00', `close_time` = '16:00' WHERE `id` = 15;