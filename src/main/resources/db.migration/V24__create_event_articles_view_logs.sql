CREATE TABLE `koin`.`event_articles_view_logs` (
  `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `event_articles_id` INT(10) UNSIGNED NOT NULL,
  `user_id` INT(10) UNSIGNED DEFAULT NULL,
  `expired_at` TIMESTAMP NULL DEFAULT NULL,
  `ip` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX idx_unique(event_articles_id, user_id))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;