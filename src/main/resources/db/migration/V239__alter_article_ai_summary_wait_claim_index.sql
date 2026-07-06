ALTER TABLE `koin`.`article_ai_summaries`
    DROP INDEX `idx_article_ai_summaries_wait_claim`,
    ADD INDEX `idx_article_ai_summaries_wait_claim` (`status`, `next_attempt_at`, `locked_until`, `updated_at`);
