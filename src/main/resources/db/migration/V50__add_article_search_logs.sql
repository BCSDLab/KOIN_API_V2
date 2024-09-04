CREATE TABLE article_search_keywords
(
    id               INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    keyword          VARCHAR(255) NOT NULL,
    weight DOUBLE NOT NULL DEFAULT 1.0,
    last_searched_at TIMESTAMP NULL,
    total_searches   INT UNSIGNED NOT NULL DEFAULT 0,
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX            idx_keyword (keyword),
    INDEX            idx_last_searched_at (last_searched_at)
);

CREATE TABLE article_search_keyword_ip_map
(
    id           INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    keyword_id   INT UNSIGNED NOT NULL,
    ip_address   VARCHAR(45) NOT NULL,
    search_count INT UNSIGNED NOT NULL DEFAULT 1,
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (keyword_id) REFERENCES article_search_keywords (id),
    INDEX        idx_ip_address (ip_address)
);
