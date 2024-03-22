CREATE TABLE `koin`.`event_articles` (
  `id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  `shop_id` INT(10) UNSIGNED NOT NULL,
  `title` VARCHAR(20) NOT NULL,
  `event_title` VARCHAR(50) NOT NULL,
  `content` TEXT NOT NULL,
  `user_id` INT(10) NOT NULL,
  `nickname` VARCHAR(50) NOT NULL,
  `thumbnail` VARCHAR(255) DEFAULT NULL,
  `hit` INT(10) NOT NULL DEFAULT '0',
  `ip` VARCHAR(45) NOT NULL,
  `start_date` DATE NOT NULL,
  `end_date` DATE NOT NULL,
  `comment_count` TINYINT(1) NOT NULL DEFAULT '0',
  `is_deleted` TINYINT(1) NOT NULL DEFAULT '0',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX pk(id),
  INDEX idx_timestamp(created_at),
  INDEX idx_is_deleted(is_deleted))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;