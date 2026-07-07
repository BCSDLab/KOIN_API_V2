CREATE TABLE `koin`.`article_ai_summary_logs`
(
    `id`              INT UNSIGNED                         NOT NULL AUTO_INCREMENT,
    `summary_id`      INT UNSIGNED                         NULL,
    `article_id`      INT UNSIGNED                         NULL,
    `board_id`        INT UNSIGNED                         NULL,
    `event_type`      VARCHAR(30)                          NOT NULL,
    `status`          VARCHAR(20)                          NULL,
    `failure_type`    VARCHAR(50)                          NULL,
    `message`         VARCHAR(500) CHARACTER SET 'utf8mb4' NULL,
    `retry_count`     INT UNSIGNED                         NULL,
    `next_attempt_at` TIMESTAMP                            NULL,
    `worker_id`       VARCHAR(100)                         NULL,
    `created_at`      TIMESTAMP                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      TIMESTAMP                            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_article_ai_summary_logs_created_at` (`created_at`),
    INDEX `idx_article_ai_summary_logs_summary_created` (`summary_id`, `created_at`),
    INDEX `idx_article_ai_summary_logs_article_created` (`article_id`, `created_at`),
    INDEX `idx_article_ai_summary_logs_type_created` (`event_type`, `failure_type`, `created_at`),
    CONSTRAINT `fk_article_ai_summary_logs_summary_id`
        FOREIGN KEY (`summary_id`) REFERENCES `koin`.`article_ai_summaries` (`id`)
            ON DELETE SET NULL
            ON UPDATE NO ACTION
);
