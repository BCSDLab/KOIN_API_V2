CREATE TABLE article_search_logs
(
    id         INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    keyword    VARCHAR(255)                        NOT NULL,
    ip_address VARCHAR(45)                         NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    INDEX      idx_keyword (keyword),
    INDEX      idx_created_at (created_at)
);
