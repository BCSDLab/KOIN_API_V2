SET @notification_fcm_add_clauses = (
    SELECT GROUP_CONCAT(required.column_ddl ORDER BY required.ordinal SEPARATOR ', ')
    FROM (
        SELECT
            1 AS ordinal,
            'is_push_success' AS column_name,
            'ADD COLUMN is_push_success TINYINT(1) NULL COMMENT ''FCM 전송 성공 여부''' AS column_ddl
        UNION ALL
        SELECT
            2,
            'fcm_error_code',
            'ADD COLUMN fcm_error_code VARCHAR(100) NULL COMMENT ''FCM 에러 코드'''
        UNION ALL
        SELECT
            3,
            'fcm_messaging_error_code',
            'ADD COLUMN fcm_messaging_error_code VARCHAR(100) NULL COMMENT ''FCM 메시징 에러 코드'''
    ) AS required
    LEFT JOIN information_schema.COLUMNS AS existing
        ON existing.TABLE_SCHEMA = DATABASE()
        AND existing.TABLE_NAME = 'notification'
        AND existing.COLUMN_NAME = required.column_name
    WHERE existing.COLUMN_NAME IS NULL
);

SET @notification_fcm_ddl = IF(
    @notification_fcm_add_clauses IS NULL,
    'DO 0',
    CONCAT(
        'ALTER TABLE notification ',
        @notification_fcm_add_clauses,
        ', ALGORITHM=INSTANT'
    )
);

PREPARE notification_fcm_stmt FROM @notification_fcm_ddl;
EXECUTE notification_fcm_stmt;
DEALLOCATE PREPARE notification_fcm_stmt;
