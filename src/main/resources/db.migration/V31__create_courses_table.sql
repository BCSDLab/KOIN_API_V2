CREATE TABLE `courses`
(
    `id`         int(10) unsigned NOT NULL AUTO_INCREMENT,
    `region`     varchar(10) COLLATE utf8mb4_bin NOT NULL,
    `bus_type`   varchar(15) COLLATE utf8mb4_bin NOT NULL,
    `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin