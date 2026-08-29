-- Immutable follow-up migration for the outbox worker lease.
ALTER TABLE `team_recruitment_outbox_event`
    ADD COLUMN `locked_until` TIMESTAMP NULL DEFAULT NULL AFTER `next_attempt_at`,
    ADD COLUMN `worker_id` VARCHAR(100) NULL DEFAULT NULL AFTER `locked_until`,
    ADD KEY `idx_team_recruitment_outbox_claim`
        (`status`, `next_attempt_at`, `locked_until`, `attempt_count`, `id`);
