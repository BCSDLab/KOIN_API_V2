ALTER TABLE `coop_shop`
    ADD COLUMN `coop_name_id` INT UNSIGNED comment '생협 운영장 고유 id',
    ADD CONSTRAINT `coop_name_fk_id` FOREIGN KEY (`coop_name_id`) REFERENCES `coop_names` (`id`) ON DELETE CASCADE;

UPDATE `coop_shop` AS cs
    JOIN `coop_names` AS cn
ON cs.`name` = cn.`name`
    SET cs.`coop_name_id` = cn.`id`;

ALTER TABLE `coop_shop`
    DROP COLUMN `name`;
