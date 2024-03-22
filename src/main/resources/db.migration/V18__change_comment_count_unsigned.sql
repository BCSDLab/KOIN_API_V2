ALTER TABLE `koin`.`articles`
CHANGE COLUMN `comment_count` `comment_count` TINYINT(1) UNSIGNED NOT NULL DEFAULT '0';

ALTER TABLE `koin`.`lost_items`
CHANGE COLUMN `comment_count` `comment_count` TINYINT(1) UNSIGNED NOT NULL DEFAULT '0';

ALTER TABLE `koin`.`temp_articles`
CHANGE COLUMN `comment_count` `comment_count` TINYINT(1) UNSIGNED NOT NULL DEFAULT '0';