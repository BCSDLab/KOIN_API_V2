ALTER TABLE `koin`.`notification_subscribe`
DROP FOREIGN KEY `FK_NOTIFICATION_SUBSCRIBE_ON_USER`;
ALTER TABLE `koin`.`notification_subscribe`
    ADD CONSTRAINT `FK_NOTIFICATION_SUBSCRIBE_ON_USER`
        FOREIGN KEY (`user_id`)
            REFERENCES `koin`.`users` (`id`)
            ON DELETE CASCADE
            ON UPDATE RESTRICT;

ALTER TABLE `koin`.`notification`
DROP FOREIGN KEY `FK_NOTIFICATION_ON_USER FOREIGN KEY`;
ALTER TABLE `koin`.`notification`
    ADD CONSTRAINT `FK_NOTIFICATION_ON_USER FOREIGN KEY`
        FOREIGN KEY (`users_id`)
            REFERENCES `koin`.`users` (`id`)
            ON DELETE CASCADE;
