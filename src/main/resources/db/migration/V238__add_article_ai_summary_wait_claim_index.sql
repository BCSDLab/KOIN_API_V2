ALTER TABLE `koin`.`article_ai_summaries`
    ADD INDEX `idx_article_ai_summaries_wait_claim` (`status`, `locked_until`, `updated_at`);
