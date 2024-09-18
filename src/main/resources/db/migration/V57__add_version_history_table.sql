CREATE TABLE `update_versions` (
    `id`          INT UNSIGNED    NOT NULL    AUTO_INCREMENT      PRIMARY KEY,
    `type`        VARCHAR(50)    NOT NULL    UNIQUE,
    `version`     VARCHAR(50)    NOT NULL,
    `title`       VARCHAR(255),
    `created_at`  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `update_contents` (
    `id`          INT UNSIGNED    NOT NULL    AUTO_INCREMENT      PRIMARY KEY,
    `type_id`     INT UNSIGNED    NOT NULL,
    `title`       VARCHAR(255),
    `content`     TEXT,
    `created_at`  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`type_id`) REFERENCES update_versions (`id`)
);

CREATE TABLE `update_history` (
    `id`          INT UNSIGNED    NOT NULL    AUTO_INCREMENT      PRIMARY KEY,
    `type`        VARCHAR(50)     NOT NULL,
    `version`     VARCHAR(50)     NOT NULL,
    `title`       VARCHAR(255),
    `created_at`  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

DELETE FROM `versions` WHERE `id` = 1;

INSERT INTO `update_versions` (`type`, `version`) VALUES ('ANDROID', '3.1.0');
INSERT INTO `update_versions` (`type`, `version`) VALUES ('IOS', '3.1.0');
INSERT INTO `update_contents` (`type_id`) VALUES (1);
INSERT INTO `update_contents` (`type_id`) VALUES (2);
INSERT INTO `update_history` (`type`, `version`, `created_at`) VALUES ('ANDROID', '3.1.0', '2018-04-17 15:00:00');
INSERT INTO `update_history` (`type`, `version`, `created_at`) VALUES ('IOS', '3.1.0', '2018-04-17 15:00:00');
