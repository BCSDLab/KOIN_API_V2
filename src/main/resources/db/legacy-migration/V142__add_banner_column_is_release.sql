ALTER TABLE `koin`.`banners`
    ADD COLUMN `is_web_released`            TINYINT(1)    NOT NULL comment '웹 배포 여부'               DEFAULT '0',
    ADD COLUMN `is_android_released`        TINYINT(1)    NOT NULL comment '안드로이드 배포 여부'         DEFAULT '0',
    ADD COLUMN `is_ios_released`            TINYINT(1)    NOT NULL comment 'IOS 배포 여부'             DEFAULT '0';
