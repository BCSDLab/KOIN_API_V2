ALTER TABLE `article_search_keywords`
    MODIFY COLUMN `keyword` VARCHAR(255) NOT NULL UNIQUE;

ALTER TABLE article_search_keyword_ip_map
    ADD CONSTRAINT unique_keyword_ip UNIQUE (keyword_id, ip_address),
    ADD CONSTRAINT fk_keyword_id
    FOREIGN KEY (keyword_id)
    REFERENCES article_search_keywords (id)
    ON DELETE CASCADE;

