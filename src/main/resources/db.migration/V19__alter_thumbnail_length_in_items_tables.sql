ALTER TABLE `koin`.`items`
CHANGE COLUMN `thumbnail` `thumbnail` VARCHAR(510) NULL DEFAULT NULL ;

ALTER TABLE `koin`.`lost_items`
CHANGE COLUMN `thumbnail` `thumbnail` VARCHAR(510) NULL DEFAULT NULL ;
