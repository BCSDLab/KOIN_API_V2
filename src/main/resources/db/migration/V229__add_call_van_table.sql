CREATE TABLE IF NOT EXISTS `koin`.`callvan_post`
(
    `id`        INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `author_id` INT NOT NULL COMMENT '작성자 user_id',
    `title`     VARCHAR(100) NOT NULL ,
    `departure_type`        VARCHAR(20) NOT NULL COMMENT '정문, 후문, 테니스장, 본관동, 별관동, 천안터미널, 천안역, 천안아산역, CUSTOM',
    `departure_custom_name` VARCHAR(50) NULL COMMENT 'departure_type이 CUSTOM일 때 사용',
    `arrival_type` VARCHAR(20) NOT NULL COMMENT '정문, 후문, 테니스장, 본관동, 별관동, 천안터미널, 천안역, 천안아산역, CUSTOM',
    `arrival_custom_name` VARCHAR(50) NULL COMMENT 'arrival_type이 CUSTOM일 때 사용',
    `departure_date` DATE NOT NULL,
    `departure_time` TIME NOT NULL,
    `max_participants` INT NOT NULL COMMENT '2~11명',
    `current_participants` INT NOT NULL DEFAULT 1,
    `status` VARCHAR(20) NOT NULL DEFAULT 'RECRUITING' COMMENT 'RECRUITING, CLOSED, COMPLETED',
    `chat_room_id` INT NULL,
    `is_deleted`  TINYINT(1)      NOT NULL    DEFAULT '0' COMMENT '삭제 여부',
    `created_at`  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at`  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시'
);
CREATE INDEX `idx_callvan_post_status` ON `koin`.`callvan_post` (`status`);
CREATE INDEX `idx_callvan_post_departure_date_time` ON `koin`.`callvan_post` (`departure_date`, `departure_time`);
CREATE INDEX `idx_callvan_post_author_id` ON `koin`.`callvan_post` (`author_id`);
CREATE INDEX `idx_callvan_post_departure_type` ON `koin`.`callvan_post` (`departure_type`);
CREATE INDEX `idx_callvan_post_arrival_type` ON `koin`.`callvan_post` (`arrival_type`);
CREATE INDEX `idx_callvan_post_filter_composite` ON `koin`.`callvan_post` (`status`, `departure_date`, `departure_time`);
CREATE INDEX `idx_callvan_post_location_composite` ON `koin`.`callvan_post` (`departure_type`, `arrival_type`, `status`);

CREATE TABLE IF NOT EXISTS `koin`.`callvan_participant` (
     `id` INT AUTO_INCREMENT PRIMARY KEY,
     `post_id` INT NOT NULL,
     `member_id` INT NOT NULL,
     `role` VARCHAR(20) NOT NULL DEFAULT 'PARTICIPANT' COMMENT 'AUTHOR, PARTICIPANT',
     `joined_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
     `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
     `created_at`  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
     `updated_at`  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
     UNIQUE KEY `uk_participant_post_member` (`post_id`, `member_id`)
);

CREATE INDEX `idx_participant_member_id` ON `koin`.`callvan_participant` (`member_id`);
CREATE INDEX `idx_participant_post_id` ON `koin`.`callvan_participant` (`post_id`);

CREATE TABLE IF NOT EXISTS `koin`.`callvan_chat_room` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `callvan_post_id` INT NOT NULL UNIQUE,
    `room_name` VARCHAR(100) NOT NULL COMMENT '출발지 -> 도착지 시간 인원수 형식',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
    `created_at`  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at`  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시'
);

CREATE TABLE IF NOT EXISTS `koin`.`callvan_chat_message` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `callvan_chat_room_id` INT NOT NULL,
    `sender_id` INT NOT NULL,
    `sender_nickname` VARCHAR(50) NOT NULL COMMENT '비정규화',
    `message_type` VARCHAR(20) NOT NULL DEFAULT 'TEXT' COMMENT 'TEXT, IMAGE',
    `content` TEXT NULL,
    `is_image` TINYINT(1) NOT NULL DEFAULT '0',
    `is_left_user` TINYINT(1) NOT NULL DEFAULT '0',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
    `created_at`  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at`  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시'
);

CREATE INDEX `idx_chat_message_room_created` ON `koin`.`callvan_chat_message` (`callvan_chat_room_id`, `created_at`);

CREATE TABLE IF NOT EXISTS `koin`.`callvan_notification` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `recipient_id` INT NOT NULL,
    `notification_type` VARCHAR(30) NOT NULL COMMENT 'RECRUITMENT_COMPLETE, NEW_MESSAGE, PARTICIPANT_JOINED, DEPARTURE_IMMINENT',

    `callvan_post_id` INT NULL,
    `departure_type` VARCHAR(20) NULL,
    `departure_custom_name` VARCHAR(50) NULL,
    `arrival_type` VARCHAR(20) NULL,
    `arrival_custom_name` VARCHAR(50) NULL,
    `departure_date` DATE NULL,
    `departure_time` TIME NULL,
    `current_participants` TINYINT NULL,
    `max_participants` TINYINT NULL,
    
    `sender_nickname` VARCHAR(50) NULL,
    `message_preview` VARCHAR(100) NULL,
    `callvan_chat_room_id` INT NULL,
    
    `joined_member_nickname` VARCHAR(50) NULL,
    
    `is_read` TINYINT(1) NOT NULL DEFAULT 0,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
    `created_at`  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at`  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시'
);

CREATE INDEX `idx_notification_recipient_read` ON `koin`.`callvan_notification` (`recipient_id`, `is_read`);
