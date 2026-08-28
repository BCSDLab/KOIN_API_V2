CREATE TABLE `team_recruitment` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `author_id` INT UNSIGNED NOT NULL,
    `category` VARCHAR(32) NOT NULL,
    `title` VARCHAR(50) NOT NULL,
    `meeting_type` VARCHAR(16) NOT NULL,
    `activity_start_date` DATE NOT NULL,
    `activity_end_date` DATE NOT NULL,
    `deadline_date` DATE NOT NULL,
    `recruitment_type` VARCHAR(16) NOT NULL,
    `max_participants` INT UNSIGNED NOT NULL,
    `current_participants` INT UNSIGNED NOT NULL DEFAULT 0,
    `description` VARCHAR(1000) NOT NULL,
    `related_url` VARCHAR(2048) DEFAULT NULL,
    `qualification` VARCHAR(500) DEFAULT NULL,
    `status` VARCHAR(16) NOT NULL DEFAULT 'RECRUITING',
    `deleted_at` TIMESTAMP NULL DEFAULT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_team_recruitment_status_deadline` (`status`, `deadline_date`, `created_at`, `id`),
    KEY `idx_team_recruitment_author_status_created` (`author_id`, `status`, `created_at`, `id`),
    CONSTRAINT `fk_team_recruitment_author_id`
        FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `chk_team_recruitment_category`
        CHECK (`category` IN ('CONTEST', 'EXTERNAL_ACTIVITY', 'STUDY', 'PROJECT', 'OTHER')),
    CONSTRAINT `chk_team_recruitment_meeting_type`
        CHECK (`meeting_type` IN ('ONLINE', 'OFFLINE', 'MIXED')),
    CONSTRAINT `chk_team_recruitment_type`
        CHECK (`recruitment_type` IN ('ROLE_BASED', 'GENERAL')),
    CONSTRAINT `chk_team_recruitment_status`
        CHECK (`status` IN ('RECRUITING', 'CLOSED', 'DELETED')),
    CONSTRAINT `chk_team_recruitment_deleted_at`
        CHECK (
            (`status` = 'DELETED' AND `deleted_at` IS NOT NULL)
            OR (`status` <> 'DELETED' AND `deleted_at` IS NULL)
        ),
    CONSTRAINT `chk_team_recruitment_dates`
        CHECK (`deadline_date` <= `activity_start_date` AND `activity_start_date` <= `activity_end_date`),
    CONSTRAINT `chk_team_recruitment_max_participants`
        CHECK (`max_participants` BETWEEN 1 AND 10),
    CONSTRAINT `chk_team_recruitment_current_participants`
        CHECK (`current_participants` <= `max_participants`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `team_recruitment_role` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `recruitment_id` INT UNSIGNED NOT NULL,
    `name` VARCHAR(10) NOT NULL,
    `max_participants` INT UNSIGNED NOT NULL,
    `current_participants` INT UNSIGNED NOT NULL DEFAULT 0,
    `display_order` INT UNSIGNED NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_team_recruitment_role_id_recruitment` (`id`, `recruitment_id`),
    UNIQUE KEY `uk_team_recruitment_role_recruitment_name` (`recruitment_id`, `name`),
    UNIQUE KEY `uk_team_recruitment_role_recruitment_order` (`recruitment_id`, `display_order`),
    CONSTRAINT `fk_team_recruitment_role_recruitment_id`
        FOREIGN KEY (`recruitment_id`) REFERENCES `team_recruitment` (`id`) ON DELETE CASCADE,
    CONSTRAINT `chk_team_recruitment_role_max_participants`
        CHECK (`max_participants` BETWEEN 1 AND 10),
    CONSTRAINT `chk_team_recruitment_role_current_participants`
        CHECK (`current_participants` <= `max_participants`),
    CONSTRAINT `chk_team_recruitment_role_display_order`
        CHECK (`display_order` BETWEEN 1 AND 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `team_recruitment_profile` (
    `user_id` INT UNSIGNED NOT NULL,
    `profile_nickname` VARCHAR(20) NOT NULL,
    `preferred_role` VARCHAR(20) NOT NULL,
    `self_introduction` VARCHAR(1000) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`),
    CONSTRAINT `fk_team_recruitment_profile_user_id`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `team_recruitment_profile_skill` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `profile_user_id` INT UNSIGNED NOT NULL,
    `skill` VARCHAR(20) NOT NULL,
    `display_order` INT UNSIGNED NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_team_recruitment_profile_skill_user_order` (`profile_user_id`, `display_order`),
    CONSTRAINT `fk_team_recruitment_profile_skill_profile_user_id`
        FOREIGN KEY (`profile_user_id`) REFERENCES `team_recruitment_profile` (`user_id`) ON DELETE CASCADE,
    CONSTRAINT `chk_team_recruitment_profile_skill_display_order`
        CHECK (`display_order` >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `team_recruitment_profile_activity` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `profile_user_id` INT UNSIGNED NOT NULL,
    `title` VARCHAR(50) NOT NULL,
    `started_at` DATE NOT NULL,
    `ended_at` DATE DEFAULT NULL,
    `is_ongoing` TINYINT(1) NOT NULL DEFAULT 0,
    `description` VARCHAR(500) NOT NULL,
    `display_order` INT UNSIGNED NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_team_recruitment_profile_activity_user_order` (`profile_user_id`, `display_order`),
    CONSTRAINT `fk_team_recruitment_profile_activity_profile_user_id`
        FOREIGN KEY (`profile_user_id`) REFERENCES `team_recruitment_profile` (`user_id`) ON DELETE CASCADE,
    CONSTRAINT `chk_team_recruitment_profile_activity_ongoing`
        CHECK (
            (`is_ongoing` = 1 AND `ended_at` IS NULL)
            OR (`is_ongoing` = 0 AND `ended_at` IS NOT NULL AND `started_at` <= `ended_at`)
        ),
    CONSTRAINT `chk_team_recruitment_profile_activity_display_order`
        CHECK (`display_order` >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `team_recruitment_application` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `recruitment_id` INT UNSIGNED NOT NULL,
    `applicant_id` INT UNSIGNED NOT NULL,
    `role_id` INT UNSIGNED DEFAULT NULL,
    `motivation` VARCHAR(1000) NOT NULL,
    `availability` VARCHAR(100) NOT NULL,
    `status` VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    `profile_snapshot` JSON NOT NULL,
    `snapshot_version` INT UNSIGNED NOT NULL DEFAULT 1,
    `decision_reason` VARCHAR(32) DEFAULT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_team_recruitment_application_id_recruitment` (`id`, `recruitment_id`),
    UNIQUE KEY `uk_team_recruitment_application_recruitment_applicant` (`recruitment_id`, `applicant_id`),
    KEY `idx_team_recruitment_application_applicant_created` (`applicant_id`, `created_at`, `id`),
    KEY `idx_team_recruitment_application_recruitment_status` (`recruitment_id`, `status`, `created_at`, `id`),
    KEY `idx_team_recruitment_application_role_status` (`role_id`, `status`),
    CONSTRAINT `fk_team_recruitment_application_recruitment_id`
        FOREIGN KEY (`recruitment_id`) REFERENCES `team_recruitment` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_team_recruitment_application_applicant_id`
        FOREIGN KEY (`applicant_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_team_recruitment_application_role_recruitment`
        FOREIGN KEY (`role_id`, `recruitment_id`)
        REFERENCES `team_recruitment_role` (`id`, `recruitment_id`) ON DELETE RESTRICT,
    CONSTRAINT `chk_team_recruitment_application_status`
        CHECK (`status` IN ('PENDING', 'ACCEPTED', 'REJECTED')),
    CONSTRAINT `chk_team_recruitment_application_snapshot_version`
        CHECK (`snapshot_version` >= 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `team_recruitment_chat_room` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `recruitment_id` INT UNSIGNED NOT NULL,
    `room_scope_key` VARCHAR(64) NOT NULL,
    `room_type` VARCHAR(16) NOT NULL,
    `application_id` INT UNSIGNED DEFAULT NULL,
    `status` VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_team_recruitment_chat_room_id_recruitment` (`id`, `recruitment_id`),
    UNIQUE KEY `uk_team_recruitment_chat_room_recruitment_scope` (`recruitment_id`, `room_scope_key`),
    UNIQUE KEY `uk_team_recruitment_chat_room_recruitment_application_type`
        (`recruitment_id`, `application_id`, `room_type`),
    KEY `idx_team_recruitment_chat_room_recruitment_status` (`recruitment_id`, `status`),
    CONSTRAINT `fk_team_recruitment_chat_room_recruitment_id`
        FOREIGN KEY (`recruitment_id`) REFERENCES `team_recruitment` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_team_recruitment_chat_room_application_recruitment`
        FOREIGN KEY (`application_id`, `recruitment_id`)
        REFERENCES `team_recruitment_application` (`id`, `recruitment_id`) ON DELETE RESTRICT,
    CONSTRAINT `chk_team_recruitment_chat_room_type`
        CHECK (`room_type` IN ('TEAM', 'DIRECT')),
    CONSTRAINT `chk_team_recruitment_chat_room_status`
        CHECK (`status` IN ('ACTIVE', 'READ_ONLY')),
    CONSTRAINT `chk_team_recruitment_chat_room_application_scope`
        CHECK (
            (`room_type` = 'TEAM' AND `application_id` IS NULL AND `room_scope_key` = 'TEAM')
            OR (`room_type` = 'DIRECT' AND `application_id` IS NOT NULL AND `room_scope_key` <> 'TEAM')
        )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `team_recruitment_chat_member` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `chat_room_id` INT UNSIGNED NOT NULL,
    `user_id` INT UNSIGNED NOT NULL,
    `last_read_message_id` INT UNSIGNED DEFAULT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_team_recruitment_chat_member_room_user` (`chat_room_id`, `user_id`),
    KEY `idx_team_recruitment_chat_member_user_room` (`user_id`, `chat_room_id`),
    CONSTRAINT `fk_team_recruitment_chat_member_room_id`
        FOREIGN KEY (`chat_room_id`) REFERENCES `team_recruitment_chat_room` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_team_recruitment_chat_member_user_id`
        FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `team_recruitment_chat_message` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `chat_room_id` INT UNSIGNED NOT NULL,
    `sender_id` INT UNSIGNED NOT NULL,
    `sender_nickname` VARCHAR(50) NOT NULL,
    `content` TEXT NOT NULL,
    `is_image` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_team_recruitment_chat_message_id_room` (`id`, `chat_room_id`),
    KEY `idx_team_recruitment_chat_message_room_id_id` (`chat_room_id`, `id`),
    CONSTRAINT `fk_team_recruitment_chat_message_chat_room_id`
        FOREIGN KEY (`chat_room_id`) REFERENCES `team_recruitment_chat_room` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_team_recruitment_chat_message_sender_id`
        FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `chk_team_recruitment_chat_message_content`
        CHECK (CHAR_LENGTH(TRIM(`content`)) > 0),
    CONSTRAINT `chk_team_recruitment_chat_message_is_image`
        CHECK (`is_image` IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

ALTER TABLE `team_recruitment_chat_member`
    ADD CONSTRAINT `fk_team_recruitment_chat_member_last_read_message`
        FOREIGN KEY (`last_read_message_id`, `chat_room_id`)
        REFERENCES `team_recruitment_chat_message` (`id`, `chat_room_id`) ON DELETE RESTRICT;

CREATE TABLE `team_recruitment_notification` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `recipient_id` INT UNSIGNED NOT NULL,
    `type` VARCHAR(32) NOT NULL,
    `target_type` VARCHAR(32) NOT NULL,
    `message_preview` VARCHAR(255) NOT NULL,
    `sender_nickname` VARCHAR(50) DEFAULT NULL,
    `recruitment_id` INT UNSIGNED NOT NULL,
    `application_id` INT UNSIGNED DEFAULT NULL,
    `chat_room_id` INT UNSIGNED DEFAULT NULL,
    `read_at` TIMESTAMP NULL DEFAULT NULL,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_team_recruitment_notification_recipient_deleted_id` (`recipient_id`, `is_deleted`, `id`),
    KEY `idx_team_recruitment_notification_recipient_deleted_read_id`
        (`recipient_id`, `is_deleted`, `read_at`, `id`),
    CONSTRAINT `fk_team_recruitment_notification_recipient_id`
        FOREIGN KEY (`recipient_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_team_recruitment_notification_recruitment_id`
        FOREIGN KEY (`recruitment_id`) REFERENCES `team_recruitment` (`id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_team_recruitment_notification_application_recruitment`
        FOREIGN KEY (`application_id`, `recruitment_id`)
        REFERENCES `team_recruitment_application` (`id`, `recruitment_id`) ON DELETE RESTRICT,
    CONSTRAINT `fk_team_recruitment_notification_chat_room_recruitment`
        FOREIGN KEY (`chat_room_id`, `recruitment_id`)
        REFERENCES `team_recruitment_chat_room` (`id`, `recruitment_id`) ON DELETE RESTRICT,
    CONSTRAINT `chk_team_recruitment_notification_type`
        CHECK (`type` IN (
            'NEW_APPLICATION',
            'APPLICATION_ACCEPTED',
            'APPLICATION_REJECTED',
            'RECRUITMENT_CLOSED',
            'RECRUITMENT_DELETED',
            'NEW_CHAT_MESSAGE'
        )),
    CONSTRAINT `chk_team_recruitment_notification_target_type`
        CHECK (`target_type` IN ('APPLICANT_MANAGEMENT', 'CHAT_ROOM', 'MY_APPLICATIONS', 'NONE')),
    CONSTRAINT `chk_team_recruitment_notification_is_deleted`
        CHECK (`is_deleted` IN (0, 1)),
    CONSTRAINT `chk_team_recruitment_notification_sender`
        CHECK (
            (`type` = 'NEW_CHAT_MESSAGE' AND `sender_nickname` IS NOT NULL)
            OR (`type` <> 'NEW_CHAT_MESSAGE' AND `sender_nickname` IS NULL)
        )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `team_recruitment_outbox_event` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `event_key` VARCHAR(255) NOT NULL,
    `event_type` VARCHAR(64) NOT NULL,
    `aggregate_type` VARCHAR(64) NOT NULL,
    `aggregate_id` INT UNSIGNED NOT NULL,
    `payload` JSON NOT NULL,
    `status` VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    `attempt_count` INT UNSIGNED NOT NULL DEFAULT 0,
    `next_attempt_at` TIMESTAMP NULL DEFAULT NULL,
    `published_at` TIMESTAMP NULL DEFAULT NULL,
    `last_error` VARCHAR(500) DEFAULT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_team_recruitment_outbox_event_key` (`event_key`),
    KEY `idx_team_recruitment_outbox_status_next_attempt` (`status`, `next_attempt_at`, `id`),
    CONSTRAINT `chk_team_recruitment_outbox_status`
        CHECK (`status` IN ('PENDING', 'PROCESSING', 'PUBLISHED', 'FAILED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
