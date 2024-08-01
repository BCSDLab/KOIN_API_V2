ALTER TABLE `koin`.`device`
    ADD INDEX `FK_DEVICE_ON_USER_ID_idx` (`user_id` ASC) VISIBLE;
;
ALTER TABLE `koin`.`device`
    ADD CONSTRAINT `FK_DEVICE_ON_USER_ID`
        FOREIGN KEY (`user_id`)
            REFERENCES `koin`.`users` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION;


ALTER TABLE `koin`.`access_history`
    ADD INDEX `FK_ACCESS_HISTORY_ON_DEVICE_ID_idx` (`device_id` ASC) VISIBLE,
ADD INDEX `FK_ACCESS_HISTORY_ON_VARIABLE_ID_idx` (`variable_id` ASC) VISIBLE;
;
ALTER TABLE `koin`.`access_history`
    ADD CONSTRAINT `FK_ACCESS_HISTORY_ON_DEVICE_ID`
        FOREIGN KEY (`device_id`)
            REFERENCES `koin`.`device` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION,
ADD CONSTRAINT `FK_ACCESS_HISTORY_ON_VARIABLE_ID`
  FOREIGN KEY (`variable_id`)
  REFERENCES `koin`.`abtest_variable` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;


ALTER TABLE `koin`.`access_history_abtest_variable`
    ADD INDEX `FK_ACCESS_HISTORY_ABTEST_VARIABLE_ON_ACCESS_HISTORY_ID_idx` (`access_history_id` ASC) VISIBLE,
ADD INDEX `FK_ACCESS_HISTORY_ABTEST_VARIABLE_ON_ACCESS_VARIABLE_ID_idx` (`variable_id` ASC) VISIBLE;
;
ALTER TABLE `koin`.`access_history_abtest_variable`
    ADD CONSTRAINT `FK_ACCESS_HISTORY_ABTEST_VARIABLE_ON_ACCESS_HISTORY_ID`
        FOREIGN KEY (`access_history_id`)
            REFERENCES `koin`.`access_history` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION,
ADD CONSTRAINT `FK_ACCESS_HISTORY_ABTEST_VARIABLE_ON_ACCESS_VARIABLE_ID`
  FOREIGN KEY (`variable_id`)
  REFERENCES `koin`.`abtest_variable` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;


ALTER TABLE `koin`.`abtest_variable`
    ADD INDEX `FK_ABTEST_VARIABLE_ON_ABTEST_ID_idx` (`abtest_id` ASC) VISIBLE;
;
ALTER TABLE `koin`.`abtest_variable`
    ADD CONSTRAINT `FK_ABTEST_VARIABLE_ON_ABTEST_ID`
        FOREIGN KEY (`abtest_id`)
            REFERENCES `koin`.`abtest` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION;


ALTER TABLE `koin`.`abtest`
    CHANGE COLUMN `is_active` `is_active` TINYINT(1) NOT NULL DEFAULT '1' ,
    ADD INDEX `FK_ABTEST_ON_WINNER_ID_idx` (`winner_id` ASC) VISIBLE;
;
ALTER TABLE `koin`.`abtest`
    ADD CONSTRAINT `FK_ABTEST_ON_WINNER_ID`
        FOREIGN KEY (`winner_id`)
            REFERENCES `koin`.`abtest_variable` (`id`)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION;
