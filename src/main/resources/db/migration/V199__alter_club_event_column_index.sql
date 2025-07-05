ALTER TABLE `koin`.`club_event`
    ADD COLUMN `notified_one_hour` TINYINT(1) NOT NULL COMMENT '1시간 전 알림 발송 여부',
    ADD INDEX `idx_notified_startdate` (`notified_one_hour`, `start_date`);
