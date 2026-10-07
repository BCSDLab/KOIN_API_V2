ALTER TABLE dining_soldout_report_sequence
    ADD COLUMN delivery_cooldown_until DATETIME(6) NULL;

-- Existing reports are deliberately not backfilled: a historical Slack binding is unknown.
CREATE TABLE dining_soldout_report_delivery_target (
    report_id INT UNSIGNED NOT NULL,
    desired_sequence BIGINT NOT NULL,
    desired_snapshot LONGTEXT NOT NULL,
    confirmed_sequence BIGINT NOT NULL DEFAULT 0,
    workspace_id LONGTEXT NULL,
    channel_id LONGTEXT NULL,
    message_ts LONGTEXT NULL,
    active_delivery_id BINARY(16) NULL,
    hold_reason VARCHAR(16) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (report_id),
    KEY idx_dining_delivery_target_work (hold_reason, report_id),
    CONSTRAINT fk_dining_delivery_target_report FOREIGN KEY (report_id)
        REFERENCES dining_soldout_report(id) ON DELETE RESTRICT,
    CONSTRAINT fk_dining_delivery_target_change FOREIGN KEY (desired_sequence)
        REFERENCES dining_soldout_report_change(sequence) ON DELETE RESTRICT,
    CONSTRAINT chk_dining_delivery_target_sequence CHECK (
        desired_sequence > 0 AND confirmed_sequence >= 0 AND confirmed_sequence <= desired_sequence
    ),
    CONSTRAINT chk_dining_delivery_target_snapshot CHECK (JSON_VALID(desired_snapshot)),
    CONSTRAINT chk_dining_delivery_target_routing CHECK (
        (workspace_id IS NULL AND channel_id IS NULL AND message_ts IS NULL)
        OR (workspace_id IS NOT NULL AND CHAR_LENGTH(workspace_id) > 0
            AND channel_id IS NOT NULL AND CHAR_LENGTH(channel_id) > 0
            AND (message_ts IS NULL OR CHAR_LENGTH(message_ts) > 0))
    ),
    CONSTRAINT chk_dining_delivery_target_hold CHECK (
        hold_reason IS NULL OR hold_reason IN ('LEGACY', 'REJECTED', 'CONFLICT')
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE dining_soldout_report_delivery (
    id BINARY(16) NOT NULL,
    report_id INT UNSIGNED NOT NULL,
    source_sequence BIGINT NOT NULL,
    report_snapshot LONGTEXT NOT NULL,
    operation VARCHAR(16) NOT NULL,
    workspace_id LONGTEXT NOT NULL,
    channel_id LONGTEXT NOT NULL,
    target_message_ts LONGTEXT NULL,
    status VARCHAR(16) NOT NULL,
    active_attempt_token BINARY(16) NULL,
    send_attempt_token BINARY(16) NULL,
    next_attempt_at DATETIME(6) NULL,
    confirmed_message_ts LONGTEXT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_dining_delivery_snapshot (report_id, source_sequence),
    CONSTRAINT fk_dining_delivery_target FOREIGN KEY (report_id)
        REFERENCES dining_soldout_report_delivery_target(report_id) ON DELETE RESTRICT,
    CONSTRAINT fk_dining_delivery_change FOREIGN KEY (source_sequence)
        REFERENCES dining_soldout_report_change(sequence) ON DELETE RESTRICT,
    CONSTRAINT chk_dining_delivery_snapshot CHECK (JSON_VALID(report_snapshot)),
    CONSTRAINT chk_dining_delivery_sequence CHECK (source_sequence > 0),
    CONSTRAINT chk_dining_delivery_operation CHECK (
        (operation = 'CREATE' AND target_message_ts IS NULL)
        OR (operation = 'UPDATE' AND target_message_ts IS NOT NULL AND CHAR_LENGTH(target_message_ts) > 0)
    ),
    CONSTRAINT chk_dining_delivery_routing CHECK (
        CHAR_LENGTH(workspace_id) > 0 AND CHAR_LENGTH(channel_id) > 0
        AND (confirmed_message_ts IS NULL OR CHAR_LENGTH(confirmed_message_ts) > 0)
    ),
    CONSTRAINT chk_dining_delivery_status CHECK (
        status IN ('QUEUED', 'IN_PROGRESS', 'UNCERTAIN', 'DELIVERED', 'NEEDS_ATTENTION')
    ),
    CONSTRAINT chk_dining_delivery_active CHECK (
        (status = 'IN_PROGRESS' AND active_attempt_token IS NOT NULL AND send_attempt_token IS NOT NULL
            AND next_attempt_at IS NULL)
        OR (status <> 'IN_PROGRESS' AND active_attempt_token IS NULL)
    ),
    CONSTRAINT chk_dining_delivery_confirmed CHECK (
        status <> 'DELIVERED' OR (confirmed_message_ts IS NOT NULL AND next_attempt_at IS NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE dining_soldout_report_delivery_attempt (
    token BINARY(16) NOT NULL,
    delivery_id BINARY(16) NOT NULL,
    mode VARCHAR(16) NOT NULL,
    send_attempt_token BINARY(16) NOT NULL,
    issued_at DATETIME(6) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    expired_at DATETIME(6) NULL,
    invalidated_at DATETIME(6) NULL,
    accepted_outcome VARCHAR(16) NULL,
    accepted_result LONGTEXT NULL,
    original_result LONGTEXT NULL,
    evidence_history LONGTEXT NOT NULL,
    result_at DATETIME(6) NULL,
    PRIMARY KEY (token),
    KEY idx_dining_delivery_attempt_origin (delivery_id, send_attempt_token, mode),
    CONSTRAINT fk_dining_delivery_attempt_delivery FOREIGN KEY (delivery_id)
        REFERENCES dining_soldout_report_delivery(id) ON DELETE RESTRICT,
    CONSTRAINT chk_dining_delivery_attempt_mode CHECK (mode IN ('SEND', 'VERIFY')),
    CONSTRAINT chk_dining_delivery_attempt_origin CHECK (mode <> 'SEND' OR send_attempt_token = token),
    CONSTRAINT chk_dining_delivery_attempt_deadline CHECK (expires_at = DATE_ADD(issued_at, INTERVAL 60 SECOND)),
    CONSTRAINT chk_dining_delivery_attempt_outcome CHECK (
        accepted_outcome IS NULL OR accepted_outcome IN ('SUCCEEDED', 'NOT_APPLIED', 'UNCERTAIN')
    ),
    CONSTRAINT chk_dining_delivery_attempt_verify CHECK (mode <> 'VERIFY' OR accepted_outcome <> 'NOT_APPLIED'),
    CONSTRAINT chk_dining_delivery_attempt_result CHECK (
        (accepted_outcome IS NULL AND accepted_result IS NULL AND result_at IS NULL)
        OR (accepted_outcome IS NOT NULL AND accepted_result IS NOT NULL AND result_at IS NOT NULL)
    ),
    CONSTRAINT chk_dining_delivery_attempt_evidence CHECK (
        JSON_VALID(evidence_history)
        AND (original_result IS NULL OR JSON_VALID(original_result))
        AND (accepted_result IS NULL OR JSON_VALID(accepted_result))
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
