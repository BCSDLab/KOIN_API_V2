CREATE TABLE `coop_semester`
(
    `id`         INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `semester`   VARCHAR(200) NOT NULL UNIQUE,
    `from_date`  DATE         NOT NULL,
    `to_date`    DATE         NOT NULL,
    `is_applied` TINYINT(1)   NOT NULL DEFAULT '1',
    `created_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

ALTER TABLE `coop_shop`
    ADD COLUMN
        `semester_id` INT UNSIGNED,
    ADD CONSTRAINT `coop_semester_fk_id` FOREIGN KEY (`semester_id`) REFERENCES `coop_semester` (`id`) ON DELETE CASCADE,
    DROP COLUMN `is_deleted`,
    DROP COLUMN `semester`;

ALTER TABLE `coop_opens`
    DROP COLUMN `is_deleted`;

ALTER TABLE `coop_opens`
    CHANGE `coop_id` `coop_shop_id` INT UNSIGNED,
    DROP CONSTRAINT `FK_COOP_ID`,
    ADD CONSTRAINT `FK_COOP_SHOP_ID` FOREIGN KEY (`coop_shop_id`) REFERENCES `coop_shop` (`id`) ON DELETE CASCADE;
