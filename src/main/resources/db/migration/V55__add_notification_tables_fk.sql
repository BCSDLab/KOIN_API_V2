ALTER TABLE `koin`.`notification`
    ADD INDEX `FK_NOTIFICATION_ON_DEVICE_idx` (`device_id` ASC) VISIBLE;

ALTER TABLE `koin`.`notification`
    ADD CONSTRAINT `FK_NOTIFICATION_ON_DEVICE`
        FOREIGN KEY (`device_id`)
            REFERENCES `koin`.`device` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION;


ALTER TABLE `koin`.`notification_subscribe`
    ADD INDEX `FK_NOTIFICATION_SUBSCRIBE_ON_DEVICE_idx` (`device_id` ASC) VISIBLE;

ALTER TABLE `koin`.`notification_subscribe`
    ADD CONSTRAINT `FK_NOTIFICATION_SUBSCRIBE_ON_DEVICE`
        FOREIGN KEY (`device_id`)
            REFERENCES `koin`.`device` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION;
