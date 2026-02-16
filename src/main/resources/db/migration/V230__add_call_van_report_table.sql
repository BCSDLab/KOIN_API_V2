CREATE TABLE IF NOT EXISTS `koin`.`callvan_report`
(
    `id`              INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `callvan_post_id` INT          NULL COMMENT '신고 접수된 콜벤팟 게시글 id',
    `reporter_id`     INT          NOT NULL COMMENT '신고자 user_id',
    `reported_id`     INT          NOT NULL COMMENT '피신고자 user_id',
    `description`     TEXT         NULL COMMENT '신고 상세 내용(상황 설명 등)',
    `status`          VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, UNDER_REVIEW, CONFIRMED, REJECTED, CANCELED',
    `reviewer_id`     INT          NULL COMMENT '운영 검토자 user_id (Admin)',
    `review_note`     VARCHAR(500) NULL COMMENT '운영 메모/판단 근거',
    `reviewed_at`     TIMESTAMP    NULL,
    `confirmed_at`    TIMESTAMP    NULL COMMENT 'CONFIRMED 시각(누적/제재 기준 시각)',
    `is_deleted`      TINYINT(1)   NOT NULL DEFAULT '0',
    `created_at`      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX `idx_callvan_report_status_created` (`status`, `created_at`),
    INDEX `idx_callvan_report_reported_status` (`reported_id`, `status`, `confirmed_at`),
    INDEX `idx_callvan_report_reporter_created` (`reporter_id`, `created_at`),
    INDEX `idx_callvan_report_post` (`callvan_post_id`)
);

CREATE TABLE IF NOT EXISTS `koin`.`callvan_report_reason`
(
    `id`          INT          NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `report_id`   INT          NOT NULL COMMENT 'callvan_report.id',
    `reason_code` VARCHAR(30)  NOT NULL COMMENT 'NO_SHOW, NON_PAYMENT, PROFANITY, OTHER',
    `custom_text` VARCHAR(200) NULL COMMENT '기타 사유 직접 입력',
    `is_deleted`  TINYINT(1)   NOT NULL DEFAULT '0',
    `created_at`  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX `idx_report_reason_report` (`report_id`),
    INDEX `idx_report_reason_code` (`reason_code`)
);
