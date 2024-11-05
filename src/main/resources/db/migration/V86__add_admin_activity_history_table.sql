CREATE TABLE `koin`.`admins_activity_history`
(
    `id`              INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 id',
    `domain_id`       INT UNSIGNED NULL COMMENT '도메인 엔티티 id',
    `admin_id`        INT UNSIGNED NOT NULL COMMENT '어드민 고유 id',
    `request_method`  VARCHAR(10) NOT NULL COMMENT 'HTTP 요청 메소드',
    `domain_name`     VARCHAR(20) NOT NULL COMMENT '도메인 이름',
    `request_message` TEXT NULL COMMENT 'HTTP 요청 메시지 바디',
    `created_at`      timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`      timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
    PRIMARY KEY (id),
    CONSTRAINT `FK_HISTORY_ON_ADMIN` FOREIGN KEY (`admin_id`) REFERENCES `admins` (`user_id`) ON DELETE CASCADE
)
