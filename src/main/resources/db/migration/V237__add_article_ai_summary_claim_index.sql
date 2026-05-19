ALTER TABLE `koin`.`article_ai_summaries`
    DROP INDEX `idx_article_ai_summaries_status_attempt`,
    ADD INDEX `idx_article_ai_summaries_claim` (`status`, `retry_count`, `next_attempt_at`, `locked_until`, `updated_at`);
