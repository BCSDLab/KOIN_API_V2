ALTER TABLE dining_soldout_report_change
    ADD COLUMN delivery_id BINARY(16) NULL,
    ADD COLUMN report_snapshot LONGTEXT NULL,
    ADD COLUMN delivery_state VARCHAR(16) NOT NULL DEFAULT 'QUEUED',
    ADD COLUMN attempt_token BINARY(16) NULL,
    ADD COLUMN expires_at DATETIME(6) NULL,
    ADD COLUMN next_attempt_at DATETIME(6) NULL,
    ADD COLUMN accepted_outcome VARCHAR(16) NULL,
    ADD UNIQUE KEY uk_dining_report_change_delivery (delivery_id),
    ADD UNIQUE KEY uk_dining_report_change_attempt (attempt_token),
    ADD KEY idx_dining_report_change_claim (delivery_state, next_attempt_at, sequence),
    ADD CONSTRAINT chk_dining_report_change_snapshot CHECK (
        report_snapshot IS NULL OR JSON_VALID(report_snapshot)
    ),
    ADD CONSTRAINT chk_dining_report_change_delivery_state CHECK (
        delivery_state IN ('QUEUED', 'IN_PROGRESS', 'DELIVERED')
    ),
    ADD CONSTRAINT chk_dining_report_change_outcome CHECK (
        accepted_outcome IS NULL OR accepted_outcome IN ('SUCCEEDED', 'FAILED')
    ),
    ADD CONSTRAINT chk_dining_report_change_lease CHECK (
        delivery_state <> 'IN_PROGRESS'
        OR (delivery_id IS NOT NULL AND report_snapshot IS NOT NULL
            AND attempt_token IS NOT NULL AND expires_at IS NOT NULL
            AND next_attempt_at IS NULL AND accepted_outcome IS NULL)
    );

-- Freeze the one-time legacy boundary with the same gate used by both application versions.
-- Writers committing after this transaction keep the QUEUED default, including old-version inserts.
START TRANSACTION;
SELECT last_sequence INTO @dining_report_delivery_migration_sequence
FROM dining_soldout_report_sequence WHERE id = 1 FOR UPDATE;

UPDATE dining_soldout_report_change c
LEFT JOIN dining_soldout_report_delivery_target t ON t.report_id = c.report_id
LEFT JOIN dining_soldout_report_delivery d ON d.report_id = c.report_id AND d.source_sequence = c.sequence
SET c.delivery_id = d.id,
    c.report_snapshot = COALESCE(d.report_snapshot,
        CASE WHEN t.desired_sequence = c.sequence THEN t.desired_snapshot ELSE NULL END),
    c.delivery_state = CASE
        WHEN t.desired_sequence = c.sequence AND t.desired_sequence > t.confirmed_sequence THEN 'QUEUED'
        ELSE 'DELIVERED'
    END
WHERE c.sequence <= @dining_report_delivery_migration_sequence;
COMMIT;
