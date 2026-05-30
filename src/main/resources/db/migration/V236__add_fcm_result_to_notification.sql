ALTER TABLE notification
    ADD COLUMN is_push_success TINYINT(1) NULL COMMENT 'FCM 전송 성공 여부',
    ADD COLUMN fcm_error_code VARCHAR(100) NULL COMMENT 'FCM 에러 코드',
    ADD COLUMN fcm_messaging_error_code VARCHAR(100) NULL COMMENT 'FCM 메시징 에러 코드';
