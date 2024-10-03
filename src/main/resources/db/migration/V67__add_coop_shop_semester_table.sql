CREATE TABLE `coop_shop_semester`
(
    `id`         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `semester`   VARCHAR(200) NOT NULL UNIQUE,
    `term`       VARCHAR(200) NOT NULL,
    `is_applied` TINYINT(1) NOT NULL DEFAULT '1',
    `created_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

ALTER TABLE `coop_shop`
    ADD COLUMN
        `semester_id` INT UNSIGNED,
    ADD CONSTRAINT `coop_shop_semester_fk_id` FOREIGN KEY (`semester_id`) REFERENCES `coop_shop_semester` (`id`),
    DROP COLUMN `is_deleted`,
    DROP COLUMN `semester`
;

ALTER TABLE `coop_opens`
    DROP COLUMN `is_deleted`;
