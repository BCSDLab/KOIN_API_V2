ALTER TABLE dining_menus
    ADD COLUMN sold_out_source VARCHAR(16) NULL,
    ADD CONSTRAINT chk_dining_sold_out_source
        CHECK (sold_out_source IS NULL OR sold_out_source IN ('COOP', 'REPORT', 'UNKNOWN'));

CREATE TABLE dining_soldout_report (
    id INT UNSIGNED NOT NULL AUTO_INCREMENT,
    dining_id INT UNSIGNED NOT NULL,
    reporter_id INT UNSIGNED NULL,
    image_url VARCHAR(2048) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    processing_type VARCHAR(32) NULL,
    processing_id BINARY(16) NULL,
    source_report_id INT UNSIGNED NULL,
    processor_workspace_id VARCHAR(64) NULL,
    processor_user_id VARCHAR(64) NULL,
    processor_name VARCHAR(80) NULL,
    processed_at DATETIME NULL,
    request_key BINARY(16) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_dining_report_student (reporter_id, dining_id),
    UNIQUE KEY uk_dining_report_request (reporter_id, request_key),
    KEY idx_dining_report_created (created_at, id),
    KEY idx_dining_report_pending (status, created_at, id),
    KEY idx_dining_report_dining (dining_id, status, id),
    KEY idx_dining_report_processing (processing_id, id),
    CONSTRAINT fk_dining_report_dining FOREIGN KEY (dining_id) REFERENCES dining_menus(id) ON DELETE RESTRICT,
    CONSTRAINT fk_dining_report_reporter FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_dining_report_source FOREIGN KEY (source_report_id) REFERENCES dining_soldout_report(id) ON DELETE RESTRICT,
    CONSTRAINT chk_dining_report_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    CONSTRAINT chk_dining_report_terminal CHECK (
        status = 'PENDING' OR (processing_type IS NOT NULL AND processing_id IS NOT NULL AND processed_at IS NOT NULL)
    ),
    CONSTRAINT chk_dining_report_result CHECK (
        (status = 'PENDING' AND processing_type IS NULL AND processing_id IS NULL AND processed_at IS NULL
            AND source_report_id IS NULL AND processor_workspace_id IS NULL AND processor_user_id IS NULL AND processor_name IS NULL)
        OR
        (status IN ('APPROVED', 'REJECTED') AND processing_type = 'MANUAL'
            AND processing_id IS NOT NULL AND processed_at IS NOT NULL AND source_report_id IS NULL
            AND processor_workspace_id IS NOT NULL AND processor_user_id IS NOT NULL AND processor_name IS NOT NULL)
        OR
        (status = 'APPROVED' AND processing_type = 'SAME_DINING_APPROVED'
            AND processing_id IS NOT NULL AND processed_at IS NOT NULL AND source_report_id IS NOT NULL
            AND processor_workspace_id IS NULL AND processor_user_id IS NULL AND processor_name IS NULL)
        OR
        (status = 'REJECTED' AND processing_type = 'COOP_PREPROCESSED'
            AND processing_id IS NOT NULL AND processed_at IS NOT NULL AND source_report_id IS NULL
            AND processor_workspace_id IS NULL AND processor_user_id IS NULL AND processor_name IS NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE dining_soldout_report_change (
    sequence BIGINT NOT NULL,
    report_id INT UNSIGNED NOT NULL,
    event_type VARCHAR(16) NOT NULL,
    status VARCHAR(16) NOT NULL,
    processing_type VARCHAR(32) NULL,
    processing_id BINARY(16) NULL,
    occurred_at DATETIME NOT NULL,
    PRIMARY KEY (sequence),
    KEY idx_dining_report_change_report (report_id, sequence),
    CONSTRAINT fk_dining_report_change_report FOREIGN KEY (report_id) REFERENCES dining_soldout_report(id) ON DELETE RESTRICT,
    CONSTRAINT chk_dining_report_change_sequence CHECK (sequence > 0),
    CONSTRAINT chk_dining_report_change_event CHECK (event_type IN ('CREATED', 'PROCESSED')),
    CONSTRAINT chk_dining_report_change_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- The row lock is held until the business transaction commits or rolls back.
CREATE TABLE dining_soldout_report_sequence (
    id TINYINT NOT NULL,
    last_sequence BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT chk_dining_report_sequence_id CHECK (id = 1),
    CONSTRAINT chk_dining_report_sequence_value CHECK (last_sequence >= 0)
) ENGINE=InnoDB;
INSERT INTO dining_soldout_report_sequence (id, last_sequence) VALUES (1, 0);
