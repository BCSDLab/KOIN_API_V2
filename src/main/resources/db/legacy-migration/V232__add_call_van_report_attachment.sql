CREATE TABLE IF NOT EXISTS `koin`.`callvan_report_attachment`
(
    `id`          INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `report_id`   INT          NOT NULL COMMENT 'callvan_report.id',
    `attachment_type` VARCHAR(30)  NOT NULL COMMENT 'IMAGE',
    `url`         VARCHAR(500)     NOT NULL COMMENT 'url',
    `is_deleted`  TINYINT(1)   NOT NULL DEFAULT '0',
    `created_at`  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX `idx_report_attachment_report` (`report_id`, `is_deleted`)
);
