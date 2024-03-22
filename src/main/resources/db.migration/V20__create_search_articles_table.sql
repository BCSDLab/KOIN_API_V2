CREATE TABLE `koin`.`search_articles` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `table_id` INT UNSIGNED NOT NULL,
  `article_id` INT UNSIGNED NOT NULL,
  `title` VARCHAR(255) NOT NULL,
  `content` TEXT,
  `user_id` INT(10) UNSIGNED,
  `nickname` VARCHAR(50) NOT NULL,
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX pk(id),
  UNIQUE INDEX idx_unique(table_id, article_id),
  INDEX idx_timestamp(created_at),
  INDEX idx_is_deleted(is_deleted),
  INDEX idx_nickname(nickname, is_deleted, created_at))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COLLATE = utf8_bin;