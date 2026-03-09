CREATE TABLE IF NOT EXISTS `koin`.`callvan_report_process`
(
    `id`               INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `report_id`        INT          NOT NULL COMMENT 'callvan_report.id',
    `processor_id`     INT          NOT NULL COMMENT '어드민 처리자',
    `process_type`     VARCHAR(50)  NOT NULL COMMENT 'WARNING, TEMPORARY_RESTRICTION_14_DAYS, PERMANENT_RESTRICTION, REJECT',
    `restricted_until` DATETIME     NULL COMMENT '14일 제한 종료일',
    `is_deleted`       TINYINT(1)   NOT NULL DEFAULT '0',
    `created_at`       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX `idx_callvan_report_process_report` (`report_id`, `is_deleted`),
    INDEX `idx_callvan_report_process_processor` (`processor_id`, `is_deleted`),
    INDEX `idx_callvan_report_process_type_until` (`process_type`, `restricted_until`, `is_deleted`)
);
