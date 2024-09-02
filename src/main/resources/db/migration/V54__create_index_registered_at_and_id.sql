SET @index_exists = (
    SELECT COUNT(1) 
    FROM information_schema.statistics 
    WHERE table_schema = 'koin' 
    AND table_name = 'koreatech_articles' 
    AND index_name = 'idx_registered_at_id'
);

SET @create_index_sql = IF(@index_exists = 0,
    'CREATE INDEX idx_registered_at_id ON koin.koreatech_articles (registered_at DESC, id DESC)',
    'SELECT ''Index already exists'' AS message'
);

PREPARE stmt FROM @create_index_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
