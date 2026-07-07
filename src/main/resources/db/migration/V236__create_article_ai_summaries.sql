CREATE TABLE `koin`.`article_ai_summaries`
(
    `id`                 INT UNSIGNED                         NOT NULL AUTO_INCREMENT,
    `article_id`         INT UNSIGNED                         NOT NULL,
    `status`             VARCHAR(20)                          NOT NULL DEFAULT 'WAIT',
    `summary_content`    MEDIUMTEXT CHARACTER SET 'utf8mb4'   NULL,
    `summarized_at`      TIMESTAMP                            NULL,
    `source_fingerprint` VARCHAR(64)                          NULL,
    `source_updated_at`  TIMESTAMP                            NULL,
    `model`              VARCHAR(100)                         NULL,
    `prompt_version`     VARCHAR(20)                          NULL,
    `retry_count`        INT UNSIGNED                         NOT NULL DEFAULT 0,
    `next_attempt_at`    TIMESTAMP                            NULL,
    `locked_until`       TIMESTAMP                            NULL,
    `worker_id`          VARCHAR(100)                         NULL,
    `failure_reason`     VARCHAR(500) CHARACTER SET 'utf8mb4' NULL,
    `is_deleted`         TINYINT(1)                           NOT NULL DEFAULT 0,
    `created_at`         TIMESTAMP                            NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`         TIMESTAMP                            NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_ai_summaries_article_id` (`article_id`),
    INDEX `idx_article_ai_summaries_status_attempt` (`status`, `next_attempt_at`, `locked_until`),
    INDEX `idx_article_ai_summaries_summarized_at` (`summarized_at`),
    CONSTRAINT `fk_article_ai_summaries_article_id`
        FOREIGN KEY (`article_id`) REFERENCES `koin`.`new_articles` (`id`)
            ON DELETE CASCADE
            ON UPDATE NO ACTION
);
