SET SQL_SAFE_UPDATES = 0;

UPDATE koin.koreatech_articles
SET registered_at =
        CASE
            WHEN registered_at REGEXP '^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}$'
                THEN STR_TO_DATE(registered_at, '%Y-%m-%d %H:%i:%s')
            WHEN registered_at REGEXP '^[0-9]{2}\.[0-9]{2}\.[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}$'
                THEN STR_TO_DATE(registered_at, '%y.%m.%d %H:%i:%s')
            WHEN registered_at REGEXP '^[0-9]{4}-[0-9]{2}-[0-9]{2}$'
                THEN STR_TO_DATE(CONCAT(registered_at, ' 00:00:00'), '%Y-%m-%d %H:%i:%s')
            ELSE registered_at
            END;

SET SQL_SAFE_UPDATES = 1;

ALTER TABLE koin.koreatech_articles
    MODIFY COLUMN `registered_at` DATETIME DEFAULT NULL COMMENT '등록 일자';
