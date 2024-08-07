CREATE TABLE `device`
(
    `id`               int unsigned NOT NULL AUTO_INCREMENT,
    `user_id`          int unsigned NOT NULL,
    `model`            varchar(100)       DEFAULT NULL,
    `os`               varchar(100)       DEFAULT NULL,
    `fcm_token`        varchar(255)       DEFAULT NULL,
    `last_accessed_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `created_at`       timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE `access_history`
(
    `id`          int unsigned NOT NULL AUTO_INCREMENT,
    `device_id`   int unsigned DEFAULT NULL,
    `public_ip`   varchar(45) NOT NULL,
    `variable_id` int unsigned NOT NULL,
    `created_at`  timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE `abtest_variable`
(
    `id`           int unsigned NOT NULL AUTO_INCREMENT,
    `abtest_id`    int unsigned NOT NULL,
    `name`         varchar(255) NOT NULL,
    `display_name` varchar(255) NOT NULL,
    `rate`         int unsigned NOT NULL,
    `count`        int unsigned NOT NULL DEFAULT '0',
    `is_before`    tinyint(1) NOT NULL DEFAULT '0',
    `created_at`   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE `access_history_abtest_variable`
(
    `id`                int unsigned NOT NULL AUTO_INCREMENT,
    `access_history_id` int unsigned NOT NULL,
    `variable_id`       int unsigned NOT NULL,
    `created_at`        timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE `abtest`
(
    `id`            int unsigned NOT NULL AUTO_INCREMENT,
    `title`         varchar(255) NOT NULL,
    `display_title` varchar(255) NOT NULL,
    `description`   varchar(255)          DEFAULT NULL,
    `creator`       varchar(50)           DEFAULT NULL,
    `team`          varchar(50)           DEFAULT NULL,
    `winner_id`     int unsigned DEFAULT NULL,
    `status`        varchar(50)  NOT NULL DEFAULT 'IN_PROGRESS',
    `created_at`    timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

