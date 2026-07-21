SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET UNIQUE_CHECKS = 0;

CREATE TABLE `abtest` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `display_title` varchar(255) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `creator` varchar(50) DEFAULT NULL,
  `team` varchar(50) DEFAULT NULL,
  `winner_id` int unsigned DEFAULT NULL,
  `status` varchar(50) NOT NULL DEFAULT 'IN_PROGRESS',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `title_UNIQUE` (`title`),
  KEY `FK_ABTEST_ON_WINNER_ID_idx` (`winner_id`),
  CONSTRAINT `FK_ABTEST_ON_WINNER_ID` FOREIGN KEY (`winner_id`) REFERENCES `abtest_variable` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `abtest_variable` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `abtest_id` int unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `display_name` varchar(255) NOT NULL,
  `rate` int unsigned NOT NULL,
  `count` int NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FK_ABTEST_VARIABLE_ON_ABTEST_ID_idx` (`abtest_id`),
  CONSTRAINT `FK_ABTEST_VARIABLE_ON_ABTEST_ID` FOREIGN KEY (`abtest_id`) REFERENCES `abtest` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `access_history` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `device_id` int unsigned DEFAULT NULL,
  `last_accessed_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FK_ACCESS_HISTORY_ON_DEVICE_ID_idx` (`device_id`),
  CONSTRAINT `FK_ACCESS_HISTORY_ON_DEVICE_ID` FOREIGN KEY (`device_id`) REFERENCES `device` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `access_history_abtest_variable` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `access_history_id` int unsigned NOT NULL,
  `variable_id` int unsigned NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FK_ACCESS_HISTORY_ABTEST_VARIABLE_ON_ACCESS_HISTORY_ID_idx` (`access_history_id`),
  KEY `FK_ACCESS_HISTORY_ABTEST_VARIABLE_ON_ACCESS_VARIABLE_ID_idx` (`variable_id`),
  CONSTRAINT `FK_ACCESS_HISTORY_ABTEST_VARIABLE_ON_ACCESS_HISTORY_ID` FOREIGN KEY (`access_history_id`) REFERENCES `access_history` (`id`),
  CONSTRAINT `FK_ACCESS_HISTORY_ABTEST_VARIABLE_ON_ACCESS_VARIABLE_ID` FOREIGN KEY (`variable_id`) REFERENCES `abtest_variable` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `activities` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'activities 고유 id',
  `title` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_bin NOT NULL COMMENT '활동명',
  `description` text CHARACTER SET utf8mb3 COLLATE utf8_bin COMMENT '활동 설명',
  `image_urls` text CHARACTER SET utf8mb3 COLLATE utf8_bin COMMENT '이미지 링크',
  `date` date NOT NULL COMMENT '활동 일자',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

CREATE TABLE `admins` (
  `user_id` int unsigned NOT NULL COMMENT 'user 고유 id',
  `team_type` varchar(255) NOT NULL COMMENT '팀 타입',
  `track_type` varchar(255) NOT NULL COMMENT '트랙 타입',
  `can_create_admin` tinyint(1) NOT NULL DEFAULT '0' COMMENT '어드민 계정 생성 권한',
  `super_admin` tinyint(1) NOT NULL DEFAULT '0' COMMENT '슈퍼 어드민 권한',
  `login_id` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uq_admin_login_id` (`login_id`),
  UNIQUE KEY `uq_admin_email` (`email`),
  CONSTRAINT `FK_ADMIN_ON_USER` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `admins_activity_history` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 id',
  `domain_id` int unsigned DEFAULT NULL COMMENT '도메인 엔티티 id',
  `admin_id` int unsigned NOT NULL COMMENT '어드민 고유 id',
  `request_method` varchar(10) NOT NULL COMMENT 'HTTP 요청 메소드',
  `domain_name` varchar(20) NOT NULL COMMENT '도메인 이름',
  `request_message` text COMMENT 'HTTP 요청 메시지 바디',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`),
  KEY `FK_HISTORY_ON_ADMIN` (`admin_id`),
  CONSTRAINT `FK_HISTORY_ON_ADMIN` FOREIGN KEY (`admin_id`) REFERENCES `admins` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `article_attachments` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `article_id` int unsigned NOT NULL,
  `hash` binary(32) NOT NULL,
  `url` text NOT NULL,
  `name` text NOT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_article_attachment` (`article_id`,`hash`),
  KEY `FK_ARTICLE_ATTACHMENTS_ON_ARTICLE_ID_idx` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `article_keyword_user_map` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `keyword_id` int unsigned NOT NULL,
  `user_id` int unsigned NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_keyword_user` (`keyword_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `article_keyword_user_map_ibfk_1` FOREIGN KEY (`keyword_id`) REFERENCES `article_keywords` (`id`) ON DELETE CASCADE,
  CONSTRAINT `article_keyword_user_map_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `article_keywords` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `keyword` varchar(50) NOT NULL,
  `category` varchar(20) NOT NULL DEFAULT 'KOREATECH',
  `last_used_at` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_filtered` tinyint(1) NOT NULL DEFAULT '0',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_keywords_keyword_category` (`keyword`,`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `article_search_keyword_ip_map` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `keyword_id` int unsigned NOT NULL,
  `ip_address` varchar(45) NOT NULL,
  `search_count` int unsigned NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_keyword_ip` (`keyword_id`,`ip_address`),
  KEY `idx_ip_address` (`ip_address`),
  CONSTRAINT `article_search_keyword_ip_map_ibfk_1` FOREIGN KEY (`keyword_id`) REFERENCES `article_search_keywords` (`id`),
  CONSTRAINT `fk_keyword_id` FOREIGN KEY (`keyword_id`) REFERENCES `article_search_keywords` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `article_search_keywords` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `keyword` varchar(255) NOT NULL,
  `weight` double NOT NULL DEFAULT '1',
  `last_searched_at` timestamp NULL DEFAULT NULL,
  `total_searches` int unsigned NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `keyword` (`keyword`),
  KEY `idx_keyword` (`keyword`),
  KEY `idx_last_searched_at` (`last_searched_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `article_view_logs` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'article view logs 고유 id',
  `article_id` int unsigned NOT NULL COMMENT 'article 고유 id',
  `user_id` int unsigned DEFAULT NULL COMMENT '본 사람 user 고유 id',
  `expired_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '만료 시간',
  `ip` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci NOT NULL COMMENT 'IP 주소',
  PRIMARY KEY (`id`),
  UNIQUE KEY `article_view_logs_article_id_user_id_unique` (`article_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_unicode_ci;

CREATE TABLE `banner_categories` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `name` varchar(255) NOT NULL COMMENT '배너 카테고리 이름',
  `description` varchar(255) NOT NULL COMMENT '배너 카테고리 설명',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `banners` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `title` varchar(255) NOT NULL COMMENT '배너 이름',
  `banner_category_id` int unsigned NOT NULL COMMENT '배너 카테고리 ID',
  `priority` int unsigned DEFAULT NULL COMMENT '배너 우선 순위',
  `image_url` varchar(255) NOT NULL COMMENT '배너 이미지 URL',
  `web_redirect_link` varchar(255) DEFAULT NULL COMMENT '웹 리다이렉션 URL',
  `android_redirect_link` varchar(255) DEFAULT NULL COMMENT '안드로이드 리다이렉션 URL',
  `android_minimum_version` varchar(50) DEFAULT NULL COMMENT '안드로이드 최소 버전',
  `ios_redirect_link` varchar(255) DEFAULT NULL COMMENT 'IOS 리다이렉션 URL',
  `ios_minimum_version` varchar(50) DEFAULT NULL COMMENT 'IOS 최소 버전',
  `is_active` tinyint(1) NOT NULL COMMENT '배너 활성화 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  `is_web_released` tinyint(1) NOT NULL DEFAULT '0' COMMENT '웹 배포 여부',
  `is_android_released` tinyint(1) NOT NULL DEFAULT '0' COMMENT '안드로이드 배포 여부',
  `is_ios_released` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'IOS 배포 여부',
  PRIMARY KEY (`id`),
  KEY `banner_category_id` (`banner_category_id`),
  CONSTRAINT `banners_ibfk_1` FOREIGN KEY (`banner_category_id`) REFERENCES `banner_categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `boards` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'board 고유 id',
  `name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci NOT NULL COMMENT '게시판 이름',
  `is_anonymous` tinyint(1) NOT NULL DEFAULT '0' COMMENT '익명 닉네임을 사용하는지 여부',
  `article_count` int unsigned NOT NULL DEFAULT '0' COMMENT '게시글 개수',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  `is_notice` tinyint(1) NOT NULL DEFAULT '0' COMMENT '공지사항인지 여부',
  `parent_id` int unsigned DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_unicode_ci;

CREATE TABLE `callvan_chat_message` (
  `id` int NOT NULL AUTO_INCREMENT,
  `callvan_chat_room_id` int NOT NULL,
  `sender_id` int NOT NULL,
  `sender_nickname` varchar(50) NOT NULL COMMENT '비정규화',
  `message_type` varchar(20) NOT NULL DEFAULT 'TEXT' COMMENT 'TEXT, IMAGE',
  `content` text,
  `is_image` tinyint(1) NOT NULL DEFAULT '0',
  `is_left_user` tinyint(1) NOT NULL DEFAULT '0',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (`id`),
  KEY `idx_chat_message_room_created` (`callvan_chat_room_id`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `callvan_chat_room` (
  `id` int NOT NULL AUTO_INCREMENT,
  `callvan_post_id` int NOT NULL,
  `room_name` varchar(100) NOT NULL COMMENT '출발지 -> 도착지 시간 인원수 형식',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (`id`),
  UNIQUE KEY `callvan_post_id` (`callvan_post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `callvan_notification` (
  `id` int NOT NULL AUTO_INCREMENT,
  `recipient_id` int NOT NULL,
  `notification_type` varchar(30) NOT NULL COMMENT 'RECRUITMENT_COMPLETE, NEW_MESSAGE, PARTICIPANT_JOINED, DEPARTURE_IMMINENT',
  `callvan_post_id` int DEFAULT NULL,
  `departure_type` varchar(20) DEFAULT NULL,
  `departure_custom_name` varchar(50) DEFAULT NULL,
  `arrival_type` varchar(20) DEFAULT NULL,
  `arrival_custom_name` varchar(50) DEFAULT NULL,
  `departure_date` date DEFAULT NULL,
  `departure_time` time DEFAULT NULL,
  `current_participants` int DEFAULT NULL,
  `max_participants` int DEFAULT NULL,
  `sender_nickname` varchar(50) DEFAULT NULL,
  `message_preview` varchar(100) DEFAULT NULL,
  `callvan_chat_room_id` int DEFAULT NULL,
  `joined_member_nickname` varchar(50) DEFAULT NULL,
  `is_read` tinyint(1) NOT NULL DEFAULT '0',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (`id`),
  KEY `idx_notification_recipient_read` (`recipient_id`,`is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `callvan_participant` (
  `id` int NOT NULL AUTO_INCREMENT,
  `post_id` int NOT NULL,
  `member_id` int NOT NULL,
  `role` varchar(20) NOT NULL DEFAULT 'PARTICIPANT' COMMENT 'AUTHOR, PARTICIPANT',
  `joined_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_participant_post_member` (`post_id`,`member_id`),
  KEY `idx_participant_member_id` (`member_id`),
  KEY `idx_participant_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `callvan_post` (
  `id` int NOT NULL AUTO_INCREMENT,
  `author_id` int NOT NULL COMMENT '작성자 user_id',
  `title` varchar(100) NOT NULL,
  `departure_type` varchar(20) NOT NULL COMMENT '정문, 후문, 테니스장, 본관동, 별관동, 천안터미널, 천안역, 천안아산역, CUSTOM',
  `departure_custom_name` varchar(50) DEFAULT NULL COMMENT 'departure_type이 CUSTOM일 때 사용',
  `arrival_type` varchar(20) NOT NULL COMMENT '정문, 후문, 테니스장, 본관동, 별관동, 천안터미널, 천안역, 천안아산역, CUSTOM',
  `arrival_custom_name` varchar(50) DEFAULT NULL COMMENT 'arrival_type이 CUSTOM일 때 사용',
  `departure_date` date NOT NULL,
  `departure_time` time NOT NULL,
  `max_participants` int NOT NULL COMMENT '2~11명',
  `current_participants` int NOT NULL DEFAULT '1',
  `status` varchar(20) NOT NULL DEFAULT 'RECRUITING' COMMENT 'RECRUITING, CLOSED, COMPLETED',
  `chat_room_id` int DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (`id`),
  KEY `idx_callvan_post_status` (`status`),
  KEY `idx_callvan_post_departure_date_time` (`departure_date`,`departure_time`),
  KEY `idx_callvan_post_author_id` (`author_id`),
  KEY `idx_callvan_post_departure_type` (`departure_type`),
  KEY `idx_callvan_post_arrival_type` (`arrival_type`),
  KEY `idx_callvan_post_filter_composite` (`status`,`departure_date`,`departure_time`),
  KEY `idx_callvan_post_location_composite` (`departure_type`,`arrival_type`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `callvan_report` (
  `id` int NOT NULL AUTO_INCREMENT,
  `callvan_post_id` int DEFAULT NULL COMMENT '신고 접수된 콜벤팟 게시글 id',
  `reporter_id` int NOT NULL COMMENT '신고자 user_id',
  `reported_id` int NOT NULL COMMENT '피신고자 user_id',
  `description` text COMMENT '신고 상세 내용(상황 설명 등)',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, UNDER_REVIEW, CONFIRMED, REJECTED, CANCELED',
  `reviewer_id` int DEFAULT NULL COMMENT '운영 검토자 user_id (Admin)',
  `review_note` varchar(500) DEFAULT NULL COMMENT '운영 메모/판단 근거',
  `reviewed_at` timestamp NULL DEFAULT NULL,
  `confirmed_at` timestamp NULL DEFAULT NULL COMMENT 'CONFIRMED 시각(누적/제재 기준 시각)',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_callvan_report_status_created` (`status`,`created_at`),
  KEY `idx_callvan_report_reported_status` (`reported_id`,`status`,`confirmed_at`),
  KEY `idx_callvan_report_reporter_created` (`reporter_id`,`created_at`),
  KEY `idx_callvan_report_post` (`callvan_post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `callvan_report_attachment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `report_id` int NOT NULL COMMENT 'callvan_report.id',
  `attachment_type` varchar(30) NOT NULL COMMENT 'IMAGE',
  `url` varchar(500) NOT NULL COMMENT 'url',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_report_attachment_report` (`report_id`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `callvan_report_process` (
  `id` int NOT NULL AUTO_INCREMENT,
  `report_id` int NOT NULL COMMENT 'callvan_report.id',
  `processor_id` int NOT NULL COMMENT '어드민 처리자',
  `process_type` varchar(50) NOT NULL COMMENT 'WARNING, TEMPORARY_RESTRICTION_14_DAYS, PERMANENT_RESTRICTION, REJECT',
  `restricted_until` datetime DEFAULT NULL COMMENT '14일 제한 종료일',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_callvan_report_process_report` (`report_id`,`is_deleted`),
  KEY `idx_callvan_report_process_processor` (`processor_id`,`is_deleted`),
  KEY `idx_callvan_report_process_type_until` (`process_type`,`restricted_until`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `callvan_report_reason` (
  `id` int NOT NULL AUTO_INCREMENT,
  `report_id` int NOT NULL COMMENT 'callvan_report.id',
  `reason_code` varchar(30) NOT NULL COMMENT 'NO_SHOW, NON_PAYMENT, PROFANITY, OTHER',
  `custom_text` varchar(200) DEFAULT NULL COMMENT '기타 사유 직접 입력',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_report_reason_report` (`report_id`),
  KEY `idx_report_reason_code` (`reason_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `campus_delivery_address` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '교내 배달 주소 ID',
  `campus_address_type_id` int unsigned NOT NULL COMMENT '교내 배달 주소 타입 ID',
  `full_address` varchar(255) NOT NULL COMMENT '교내 배달 전체 주소',
  `short_address` varchar(50) NOT NULL COMMENT '교내 배달 요약 주소',
  `latitude` decimal(10,8) DEFAULT NULL COMMENT '위도',
  `longitude` decimal(11,8) DEFAULT NULL COMMENT '경도',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  `address` text COMMENT '캠퍼스 배달 주소',
  PRIMARY KEY (`id`),
  KEY `idx_campus_delivery_address` (`campus_address_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `campus_delivery_address_type` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '교내 주소 타입 ID',
  `name` varchar(255) NOT NULL COMMENT '교내 주소 타입 이름',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `cart` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `user_id` int unsigned NOT NULL COMMENT '사용자 ID',
  `orderable_shop_id` int unsigned NOT NULL COMMENT '주문 가능 상점 ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_orderable_shop` (`user_id`,`orderable_shop_id`),
  KEY `idx_user_id_orderable_shop_id` (`user_id`,`orderable_shop_id`),
  KEY `idx_orderable_shop_id` (`orderable_shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `cart_menu_item` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `cart_id` int unsigned NOT NULL COMMENT '장바구니 ID',
  `orderable_shop_menu_id` int unsigned NOT NULL COMMENT '상점 메뉴 ID',
  `orderable_shop_menu_price_id` int unsigned NOT NULL COMMENT '상점 메뉴 가격 ID',
  `quantity` int unsigned NOT NULL COMMENT '메뉴 수량',
  `is_modified` tinyint(1) NOT NULL DEFAULT '0' COMMENT '메뉴 정보 변경 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  KEY `idx_cart_id` (`cart_id`,`orderable_shop_menu_id`,`orderable_shop_menu_price_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `cart_menu_item_option` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `cart_menu_item_id` int unsigned NOT NULL COMMENT '장바구니 메뉴 ID',
  `orderable_shop_menu_option_id` int unsigned NOT NULL COMMENT '상점 메뉴 옵션 ID',
  `option_name` varchar(255) NOT NULL COMMENT '옵션 이름',
  `option_price` int unsigned NOT NULL COMMENT '옵션 가격',
  `quantity` int unsigned NOT NULL COMMENT '옵션 수량',
  `is_modified` tinyint(1) NOT NULL DEFAULT '0' COMMENT '옵션 정보 변경 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  KEY `idx_cart_menu_item_id` (`cart_menu_item_id`,`orderable_shop_menu_option_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `catalog` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 id',
  `code` varchar(20) NOT NULL COMMENT '강의 코드',
  `year` varchar(20) NOT NULL COMMENT '년도',
  `lecture_name` varchar(255) NOT NULL COMMENT '강의 이름',
  `department_id` int unsigned NOT NULL COMMENT '학과 id',
  `major_id` int unsigned DEFAULT NULL COMMENT '전공 id',
  `general_education_area_id` int unsigned DEFAULT NULL COMMENT '교양 영역 id',
  `credit` int unsigned NOT NULL DEFAULT '0' COMMENT '학점',
  `course_type_id` int unsigned NOT NULL COMMENT '이수 구분 id',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  KEY `major_id` (`major_id`),
  KEY `course_type_id` (`course_type_id`),
  KEY `department_id` (`department_id`),
  KEY `general_education_area_id` (`general_education_area_id`),
  KEY `idx_catalog_code_year` (`code`,`year`),
  KEY `idx_catalog_lecture_name` (`lecture_name`,`year`),
  CONSTRAINT `catalog_ibfk_1` FOREIGN KEY (`major_id`) REFERENCES `major` (`id`),
  CONSTRAINT `catalog_ibfk_2` FOREIGN KEY (`course_type_id`) REFERENCES `course_type` (`id`),
  CONSTRAINT `catalog_ibfk_3` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`),
  CONSTRAINT `catalog_ibfk_4` FOREIGN KEY (`general_education_area_id`) REFERENCES `general_education_area` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `club` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `club_category_id` int unsigned NOT NULL COMMENT '동아리 카테고리 ID',
  `name` varchar(50) NOT NULL COMMENT '동아리 이름',
  `hits` int unsigned NOT NULL DEFAULT '0' COMMENT '조회수',
  `description` varchar(100) NOT NULL COMMENT '동아리 소개',
  `is_active` tinyint(1) NOT NULL DEFAULT '0' COMMENT '활성화 여부',
  `image_url` varchar(255) NOT NULL COMMENT '동아리 사진',
  `likes` int unsigned NOT NULL DEFAULT '0' COMMENT '동아리 좋아요 개수',
  `location` varchar(20) NOT NULL COMMENT '동아리 장소',
  `last_week_hits` int unsigned NOT NULL DEFAULT '0' COMMENT '지난 주 조회수',
  `introduction` text NOT NULL COMMENT '동아리 상세 소개',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  `is_like_hidden` tinyint(1) NOT NULL DEFAULT '0' COMMENT '좋아요 숨김 여부',
  `normalized_name` varchar(50) NOT NULL COMMENT '검색용 정규화 이름',
  PRIMARY KEY (`id`),
  KEY `club_category_id` (`club_category_id`),
  KEY `idx_club_normalized_name` (`normalized_name`),
  CONSTRAINT `club_ibfk_1` FOREIGN KEY (`club_category_id`) REFERENCES `club_category` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `club_category` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `name` varchar(255) NOT NULL COMMENT '동아리 카테고리 이름',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `club_event` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '동아리 행사 고유 ID',
  `club_id` int unsigned NOT NULL COMMENT '동아리 고유 ID',
  `name` varchar(30) NOT NULL COMMENT '동아리 행사 이름',
  `start_date` datetime NOT NULL COMMENT '동아리 행사 시작 날짜',
  `end_date` datetime NOT NULL COMMENT '동아리 행사 종료 날짜',
  `introduce` varchar(70) NOT NULL COMMENT '동아리 행사 간단 소개',
  `content` text COMMENT '동아리 행사 상세 설명',
  `notified_before_one_hour` tinyint(1) NOT NULL COMMENT '1시간 전 알림 발송 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (`id`),
  KEY `fk_club_event_club` (`club_id`),
  KEY `idx_notified_startdate` (`notified_before_one_hour`,`start_date`),
  CONSTRAINT `fk_club_event_club` FOREIGN KEY (`club_id`) REFERENCES `club` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `club_event_image` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '동아리 행사 이미지 고유 ID',
  `club_event_id` int unsigned NOT NULL COMMENT '동아리 행사 고유 ID',
  `image_url` varchar(255) NOT NULL COMMENT '이미지 URL',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (`id`),
  KEY `club_event_id` (`club_event_id`),
  CONSTRAINT `club_event_image_ibfk_1` FOREIGN KEY (`club_event_id`) REFERENCES `club_event` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `club_event_subscription` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '동아리 모집 고유 ID',
  `event_id` int unsigned NOT NULL COMMENT '동아리 이벤트 고유 ID',
  `user_id` int unsigned NOT NULL COMMENT '유저 고유 ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_event_user` (`event_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `club_event_subscription_ibfk_1` FOREIGN KEY (`event_id`) REFERENCES `club_event` (`id`) ON DELETE CASCADE,
  CONSTRAINT `club_event_subscription_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `club_hot` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `club_id` int unsigned NOT NULL COMMENT '인기 동아리 고유 ID',
  `ranking` int unsigned NOT NULL COMMENT '순위',
  `period_hits` int unsigned NOT NULL DEFAULT '0' COMMENT '기간 조회수',
  `start_date` date NOT NULL COMMENT '집계 시작일',
  `end_date` date NOT NULL COMMENT '집계 종료일',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  KEY `club_id` (`club_id`),
  CONSTRAINT `club_hot_ibfk_1` FOREIGN KEY (`club_id`) REFERENCES `club` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `club_like` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `club_id` int unsigned NOT NULL COMMENT '동아리 고유 ID',
  `user_id` int unsigned NOT NULL COMMENT '유저 ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  KEY `club_id` (`club_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `club_like_ibfk_1` FOREIGN KEY (`club_id`) REFERENCES `club` (`id`),
  CONSTRAINT `club_like_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `club_manager` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `club_id` int unsigned NOT NULL COMMENT '동아리 고유 ID',
  `user_id` int unsigned NOT NULL COMMENT '동아리 관리자 유저 ID',
  PRIMARY KEY (`id`),
  KEY `club_id` (`club_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `club_manager_ibfk_1` FOREIGN KEY (`club_id`) REFERENCES `club` (`id`),
  CONSTRAINT `club_manager_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `club_qna` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `club_id` int unsigned NOT NULL COMMENT '동아리 고유 ID',
  `author_id` int unsigned DEFAULT NULL COMMENT '작성자 ID',
  `parent_id` int unsigned DEFAULT NULL COMMENT '부모 qna ID',
  `content` varchar(255) NOT NULL COMMENT '내용',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `is_manager` tinyint(1) NOT NULL COMMENT '관리자 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  KEY `club_id` (`club_id`),
  KEY `author_id` (`author_id`),
  KEY `parent_id` (`parent_id`),
  CONSTRAINT `club_qna_ibfk_1` FOREIGN KEY (`club_id`) REFERENCES `club` (`id`),
  CONSTRAINT `club_qna_ibfk_2` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  CONSTRAINT `club_qna_ibfk_3` FOREIGN KEY (`parent_id`) REFERENCES `club_qna` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `club_recruitment` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '동아리 모집 고유 ID',
  `club_id` int unsigned NOT NULL COMMENT '동아리 고유 ID',
  `start_date` date DEFAULT NULL COMMENT '동아리 모집 시작 날짜',
  `end_date` date DEFAULT NULL COMMENT '동아리 모집 마감 날짜',
  `is_always_recruiting` tinyint(1) NOT NULL COMMENT '동아리 상시 모집 여부',
  `image_url` varchar(255) DEFAULT NULL COMMENT '동아리 모집 이미지',
  `content` text COMMENT '동아리 모집 내용',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_club_recruitment_club_id` (`club_id`),
  CONSTRAINT `fk_club` FOREIGN KEY (`club_id`) REFERENCES `club` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `club_recruitment_subscription` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '동아리 모집 고유 ID',
  `club_id` int unsigned NOT NULL COMMENT '동아리 고유 ID',
  `user_id` int unsigned NOT NULL COMMENT '유저 고유 ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uniq_club_user` (`club_id`,`user_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `club_recruitment_subscription_ibfk_1` FOREIGN KEY (`club_id`) REFERENCES `club` (`id`) ON DELETE CASCADE,
  CONSTRAINT `club_recruitment_subscription_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `club_sns` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `club_id` int unsigned NOT NULL COMMENT '동아리 고유 ID',
  `sns_type` varchar(50) NOT NULL COMMENT '동아리 SNS 타입',
  `contact` varchar(255) NOT NULL COMMENT '동아리 SNS 연락처',
  PRIMARY KEY (`id`),
  KEY `club_id` (`club_id`),
  CONSTRAINT `club_sns_ibfk_1` FOREIGN KEY (`club_id`) REFERENCES `club` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `comments` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'comment 고유 id',
  `article_id` int unsigned NOT NULL COMMENT '게시글 고유 id',
  `content` text CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci NOT NULL COMMENT '내용',
  `user_id` int unsigned NOT NULL COMMENT '답글 user 고유 id',
  `nickname` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci NOT NULL COMMENT '답글 user 닉네임',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_unicode_ci;

CREATE TABLE `coop` (
  `user_id` int unsigned NOT NULL COMMENT '유저 id, user_type COOP으로 가져옴',
  `coop_id` varchar(255) NOT NULL,
  PRIMARY KEY (`user_id`),
  CONSTRAINT `FK_COOP_ON_USER` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `coop_names` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 id',
  `name` varchar(255) NOT NULL COMMENT '생협 운영장 이름',
  `icon_url` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `coop_opens` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'coop_open 고유 id',
  `coop_shop_id` int unsigned DEFAULT NULL,
  `type` varchar(10) DEFAULT NULL COMMENT '기타 타입(아침, 점심, 저녁)',
  `day_of_week` varchar(10) NOT NULL COMMENT '요일',
  `open_time` varchar(10) DEFAULT NULL COMMENT '오픈 시간',
  `close_time` varchar(10) DEFAULT NULL COMMENT '마감 시간',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`),
  KEY `FK_COOP_ID` (`coop_shop_id`),
  CONSTRAINT `FK_COOP_SHOP_ID` FOREIGN KEY (`coop_shop_id`) REFERENCES `coop_shop` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `coop_semester` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `semester` varchar(200) NOT NULL,
  `from_date` date NOT NULL,
  `to_date` date NOT NULL,
  `is_applied` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `semester` (`semester`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `coop_shop` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'coop 고유 id',
  `phone` varchar(50) DEFAULT NULL COMMENT '생협 매장 연락처',
  `location` varchar(50) NOT NULL COMMENT '생협 매장 위치',
  `remarks` text COMMENT '특이사항',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  `semester_id` int unsigned DEFAULT NULL,
  `coop_name_id` int unsigned DEFAULT NULL COMMENT '생협 운영장 고유 id',
  PRIMARY KEY (`id`),
  KEY `coop_semester_fk_id` (`semester_id`),
  KEY `coop_name_fk_id` (`coop_name_id`),
  CONSTRAINT `coop_name_fk_id` FOREIGN KEY (`coop_name_id`) REFERENCES `coop_names` (`id`) ON DELETE CASCADE,
  CONSTRAINT `coop_semester_fk_id` FOREIGN KEY (`semester_id`) REFERENCES `coop_semester` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `course_type` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 id',
  `name` varchar(255) NOT NULL COMMENT '이수 구분 이름',
  `is_deleted` tinyint DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `courses` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `region` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `bus_type` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `department` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 id',
  `name` varchar(255) NOT NULL COMMENT '학과 이름',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `detect_graduation_calculation` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 id',
  `user_id` int unsigned DEFAULT NULL COMMENT '유저 id',
  `is_changed` tinyint(1) DEFAULT '0' COMMENT '졸업학점 계산 변경 여부',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `device` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int unsigned NOT NULL,
  `model` varchar(100) DEFAULT NULL,
  `type` varchar(100) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `FK_DEVICE_ON_USER_ID_idx` (`user_id`),
  CONSTRAINT `FK_DEVICE_ON_USER_ID` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `dining_likes` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `dining_id` int NOT NULL,
  `user_id` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `dining_menus` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'dining menus 고유 id',
  `date` date NOT NULL COMMENT '일자',
  `type` varchar(9) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci NOT NULL COMMENT '식사 유형(아침 , 점심, 저녁)',
  `place` varchar(9) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci NOT NULL COMMENT '종류(양식, 한식..)',
  `price_card` int unsigned DEFAULT NULL COMMENT '카드 금액',
  `price_cash` int unsigned DEFAULT NULL COMMENT '현금 금액',
  `kcal` int unsigned DEFAULT NULL COMMENT '칼로리',
  `menu` text CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci NOT NULL COMMENT '메뉴',
  `image_url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  `sold_out` datetime DEFAULT NULL,
  `is_changed` datetime DEFAULT NULL,
  `likes` int DEFAULT '0',
  `price` int unsigned DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_date_type_place` (`date`,`type`,`place`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_unicode_ci;

CREATE TABLE `event_article_thumbnail_images` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `event_id` int unsigned NOT NULL,
  `thumbnail_image` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL,
  `updated_at` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_EVENT_ARTICLE_THUMBNAIL_IMAGES_ON_EVENT_ARTICLES` (`event_id`),
  CONSTRAINT `FK_EVENT_ARTICLE_THUMBNAIL_IMAGES_ON_EVENT_ARTICLES` FOREIGN KEY (`event_id`) REFERENCES `event_articles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `event_articles` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'event articles 고유 id',
  `shop_id` int unsigned NOT NULL COMMENT 'Shop(가게) 고유 id',
  `title` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_bin NOT NULL COMMENT '제목',
  `content` text CHARACTER SET utf8mb3 COLLATE utf8_bin NOT NULL COMMENT '내용',
  `user_id` int NOT NULL COMMENT 'user(작성자) 고유 id',
  `hit` int NOT NULL DEFAULT '0' COMMENT '조회수',
  `ip` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8_bin NOT NULL COMMENT 'IP 주소',
  `start_date` date NOT NULL COMMENT '행사 시작일',
  `end_date` date NOT NULL COMMENT '행사 마감일',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`),
  UNIQUE KEY `pk` (`id`),
  KEY `idx_timestamp` (`created_at`),
  KEY `idx_is_deleted` (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

CREATE TABLE `event_articles_view_logs` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'event articles view logs 고유 id',
  `event_articles_id` int unsigned NOT NULL COMMENT 'event articles 고유 id',
  `user_id` int unsigned DEFAULT NULL COMMENT '게시물을 본 user 고유 id',
  `expired_at` timestamp NULL DEFAULT NULL COMMENT '만료 일자',
  `ip` varchar(45) CHARACTER SET utf8mb3 COLLATE utf8_bin NOT NULL COMMENT 'IP 주소',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_unique` (`event_articles_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

CREATE TABLE `general_education_area` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 id',
  `name` varchar(20) NOT NULL COMMENT '교양 영역 이름',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `koin_notice` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `article_id` int unsigned NOT NULL COMMENT '게시글 고유 ID',
  `admin_id` int unsigned NOT NULL COMMENT '어드민 고유 ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`),
  KEY `article_id` (`article_id`),
  KEY `admin_id` (`admin_id`),
  CONSTRAINT `koin_notice_ibfk_1` FOREIGN KEY (`article_id`) REFERENCES `new_articles` (`id`),
  CONSTRAINT `koin_notice_ibfk_2` FOREIGN KEY (`admin_id`) REFERENCES `admins` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `lands` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'lands 고유 id',
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci NOT NULL COMMENT '건물 이름',
  `internal_name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci NOT NULL COMMENT '건물 이름 소문자 변환 및 띄어쓰기 제거',
  `size` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '방 크기',
  `room_type` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '원룸 종류',
  `latitude` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '위도',
  `longitude` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '경도',
  `phone` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '전화번호',
  `image_urls` text CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci COMMENT '이미지 링크',
  `address` text CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci COMMENT '주소',
  `description` text CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci COMMENT '세부 사항',
  `floor` int unsigned DEFAULT NULL COMMENT '층 수',
  `deposit` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '보증금',
  `monthly_fee` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '월세',
  `charter_fee` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '전세',
  `management_fee` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '관리비',
  `opt_refrigerator` tinyint(1) NOT NULL DEFAULT '0' COMMENT '냉장고 보유 여부',
  `opt_closet` tinyint(1) NOT NULL DEFAULT '0' COMMENT '옷장 보유 여부',
  `opt_tv` tinyint(1) NOT NULL DEFAULT '0' COMMENT 'tv 보유 여부',
  `opt_microwave` tinyint(1) NOT NULL DEFAULT '0' COMMENT '전자레인지 보유 여부',
  `opt_gas_range` tinyint(1) NOT NULL DEFAULT '0' COMMENT '가스레인지 보유 여부',
  `opt_induction` tinyint(1) NOT NULL DEFAULT '0' COMMENT '인덕션 보유 여부',
  `opt_water_purifier` tinyint(1) NOT NULL DEFAULT '0' COMMENT '정수기 보유 여부',
  `opt_air_conditioner` tinyint(1) NOT NULL DEFAULT '0' COMMENT '에어컨 보유 여부',
  `opt_washer` tinyint(1) NOT NULL DEFAULT '0' COMMENT '샤워기 보유 여부',
  `opt_bed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '침대 보유 여부',
  `opt_desk` tinyint(1) NOT NULL DEFAULT '0' COMMENT '책상 보유 여부',
  `opt_shoe_closet` tinyint(1) NOT NULL DEFAULT '0' COMMENT '신발장 보유 여부',
  `opt_electronic_door_locks` tinyint(1) NOT NULL DEFAULT '0' COMMENT '전자 도어락 보유 여부',
  `opt_bidet` tinyint(1) NOT NULL DEFAULT '0' COMMENT '비데 보유 여부',
  `opt_veranda` tinyint(1) NOT NULL DEFAULT '0' COMMENT '베란다 보유 여부',
  `opt_elevator` tinyint(1) NOT NULL DEFAULT '0' COMMENT '엘레베이터 보유 여부',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_name` (`name`),
  KEY `ix_internalname` (`internal_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_unicode_ci;

CREATE TABLE `lectures` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'lectures 고유 id',
  `semester_date` varchar(10) NOT NULL,
  `code` varchar(10) NOT NULL COMMENT '강의 코드',
  `name` varchar(50) NOT NULL COMMENT '강의 이름',
  `grades` varchar(2) NOT NULL COMMENT '대상 학년',
  `class` varchar(3) NOT NULL COMMENT '강의 분반',
  `regular_number` varchar(4) NOT NULL COMMENT '수강 인원',
  `department` varchar(30) NOT NULL COMMENT '강의 학과',
  `target` varchar(200) NOT NULL,
  `professor` varchar(30) DEFAULT NULL,
  `is_english` varchar(2) NOT NULL COMMENT '영어강의 여부',
  `design_score` varchar(2) NOT NULL COMMENT '설계 학점',
  `is_elearning` varchar(2) NOT NULL COMMENT '이러닝 여부',
  `class_time` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`),
  KEY `idx_lecture_semester_code` (`semester_date`,`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3;

CREATE TABLE `lost_item_articles` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 id',
  `article_id` int unsigned NOT NULL COMMENT '게시글 id',
  `author_id` int unsigned DEFAULT NULL COMMENT '작성자 id',
  `category` varchar(255) NOT NULL COMMENT '분실물 카테고리',
  `found_place` varchar(255) NOT NULL COMMENT '습득 장소',
  `found_date` date NOT NULL COMMENT '습득 날짜',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '게시글 삭제 여부',
  `is_found` tinyint(1) NOT NULL DEFAULT '0' COMMENT '분실물 찾음 여부',
  `found_at` timestamp NULL DEFAULT NULL COMMENT '분실물 찾은 시각',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  `type` varchar(100) NOT NULL DEFAULT 'LOST' COMMENT '게시글 타입',
  `is_council` tinyint(1) NOT NULL DEFAULT '0' COMMENT '작성자 총학생회 여부',
  PRIMARY KEY (`id`),
  KEY `lost_item_article_fk_id` (`article_id`),
  KEY `lost_item_article_author_fk_id` (`author_id`),
  CONSTRAINT `lost_item_article_author_fk_id` FOREIGN KEY (`author_id`) REFERENCES `users` (`id`) ON DELETE SET NULL,
  CONSTRAINT `lost_item_article_fk_id` FOREIGN KEY (`article_id`) REFERENCES `new_articles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `lost_item_images` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 id',
  `lost_item_id` int unsigned NOT NULL COMMENT '분실물 게시글 id',
  `image_url` varchar(255) NOT NULL COMMENT '분실물 이미지 url',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '게시글 삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  KEY `lost_item_image_fk_id` (`lost_item_id`),
  CONSTRAINT `lost_item_image_fk_id` FOREIGN KEY (`lost_item_id`) REFERENCES `lost_item_articles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `lost_item_reports` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `lost_id` int unsigned NOT NULL,
  `title` varchar(50) NOT NULL,
  `content` varchar(255) NOT NULL,
  `user_id` int unsigned NOT NULL,
  `status` varchar(25) DEFAULT 'UNHANDLED',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `lost_id` (`lost_id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `lost_item_reports_ibfk_1` FOREIGN KEY (`lost_id`) REFERENCES `lost_item_articles` (`id`) ON DELETE CASCADE,
  CONSTRAINT `lost_item_reports_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `major` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 id',
  `name` varchar(255) DEFAULT NULL COMMENT '전공 이름',
  `department_id` int unsigned NOT NULL COMMENT '학과 id',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_name_department` (`name`,`department_id`),
  KEY `department_id` (`department_id`),
  CONSTRAINT `major_ibfk_1` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `members` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'members 고유 id',
  `name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8_bin NOT NULL COMMENT '이름',
  `student_number` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_bin DEFAULT NULL COMMENT '학번',
  `track_id` int unsigned NOT NULL COMMENT '소속 트랙 고유 id',
  `position` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_bin NOT NULL COMMENT '직급',
  `email` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8_bin DEFAULT NULL COMMENT '이메일',
  `image_url` text CHARACTER SET utf8mb3 COLLATE utf8_bin COMMENT '이미지 링크',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

CREATE TABLE `migrations` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `migration` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci NOT NULL,
  `batch` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_unicode_ci;

CREATE TABLE `new_articles` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'notice articles 고유 id',
  `board_id` int unsigned NOT NULL COMMENT '게시판 고유 id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '제목',
  `content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '내용',
  `hit` int unsigned NOT NULL DEFAULT '0' COMMENT '조회수',
  `is_notice` tinyint(1) NOT NULL DEFAULT '0' COMMENT '공지사항인지 여부',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`),
  KEY `FK_ARTICLES_ON_BOARD_ID_idx` (`board_id`),
  KEY `idx_articles_deleted_notice` (`is_deleted`,`is_notice`),
  FULLTEXT KEY `idx_fulltext_title` (`title`) /*!50100 WITH PARSER `ngram` */ ,
  CONSTRAINT `FK_ARTICLES_ON_BOARD_ID` FOREIGN KEY (`board_id`) REFERENCES `boards` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `new_koin_articles` (
  `id` int NOT NULL AUTO_INCREMENT,
  `article_id` int unsigned NOT NULL,
  `user_id` int unsigned DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`),
  UNIQUE KEY `article_id_UNIQUE` (`article_id`),
  KEY `FK_KOIN_ARTICLES_ON_ARTICLE_ID_idx` (`article_id`),
  CONSTRAINT `FK_KOIN_ARTICLES_ON_ARTICLE_ID` FOREIGN KEY (`article_id`) REFERENCES `new_articles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `new_koreatech_articles` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'notice articles 고유 id',
  `article_id` int unsigned NOT NULL,
  `author` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '작성자',
  `portal_num` int unsigned NOT NULL COMMENT '게시물 번호',
  `portal_hit` int unsigned NOT NULL DEFAULT '0',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '기존 게시글 url',
  `registered_at` datetime DEFAULT NULL COMMENT '등록 일자',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`),
  UNIQUE KEY `article_id_UNIQUE` (`article_id`),
  KEY `FK_KOREATECH_ARTICLES_ON_ARTICLE_ID_idx` (`article_id`),
  CONSTRAINT `FK_KOREATECH_ARTICLES_ON_ARTICLE_ID` FOREIGN KEY (`article_id`) REFERENCES `new_articles` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '고유 id',
  `app_path` varchar(255) DEFAULT NULL COMMENT '앱 url',
  `title` varchar(255) DEFAULT NULL COMMENT '제목',
  `message` varchar(255) DEFAULT NULL COMMENT '메시지 내용',
  `image_url` varchar(255) DEFAULT NULL COMMENT '이미지 url',
  `type` varchar(255) DEFAULT NULL COMMENT '알림 타입',
  `users_id` int unsigned NOT NULL COMMENT '유저 id',
  `is_read` tinyint(1) NOT NULL COMMENT '읽음 여부',
  `created_at` timestamp NOT NULL COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL COMMENT '수정 일자',
  `scheme_uri` varchar(255) DEFAULT NULL,
  `is_push_success` tinyint(1) DEFAULT NULL COMMENT 'FCM 전송 성공 여부',
  `fcm_error_code` varchar(100) DEFAULT NULL COMMENT 'FCM 에러 코드',
  `fcm_messaging_error_code` varchar(100) DEFAULT NULL COMMENT 'FCM 메시징 에러 코드',
  PRIMARY KEY (`id`),
  KEY `FK_NOTIFICATION_ON_USER FOREIGN KEY` (`users_id`),
  CONSTRAINT `FK_NOTIFICATION_ON_USER FOREIGN KEY` FOREIGN KEY (`users_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `notification_subscribe` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` timestamp NOT NULL COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL COMMENT '수정 일자',
  `subscribe_type` varchar(255) NOT NULL,
  `user_id` int unsigned NOT NULL,
  `detail_type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_id_subscribe_type_detail_type` (`user_id`,`subscribe_type`,`detail_type`),
  CONSTRAINT `FK_NOTIFICATION_SUBSCRIBE_ON_USER` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `order` (
  `id` varchar(64) NOT NULL COMMENT '주문 ID',
  `order_type` varchar(10) NOT NULL COMMENT '주문 타입',
  `phone_number` varchar(20) NOT NULL COMMENT '주문자 전화번호',
  `total_product_price` int unsigned DEFAULT NULL COMMENT '상품 총 금액',
  `total_price` int unsigned DEFAULT NULL COMMENT '주문 총 금액',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `orderable_shop_id` int unsigned DEFAULT NULL COMMENT '주문한 상점 ID',
  `user_id` int unsigned DEFAULT NULL COMMENT '주문자 사용자 ID',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_orderable_shop_id` (`orderable_shop_id`),
  CONSTRAINT `fk_order_shop` FOREIGN KEY (`orderable_shop_id`) REFERENCES `orderable_shop` (`id`),
  CONSTRAINT `fk_order_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `order_delivery` (
  `order_id` varchar(64) NOT NULL COMMENT '주문 ID',
  `address` varchar(100) NOT NULL COMMENT '배달 주소',
  `to_owner` varchar(50) DEFAULT NULL COMMENT '사장님 전달 메시지',
  `to_rider` varchar(50) DEFAULT NULL COMMENT '라이더 전달 메시지',
  `delivery_tip` int unsigned NOT NULL COMMENT '배달비',
  `provide_cutlery` tinyint(1) NOT NULL DEFAULT '0' COMMENT '수저, 포크 수령 여부',
  PRIMARY KEY (`order_id`),
  CONSTRAINT `fk_order_delivery_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `order_delivery_v2` (
  `order_id` int unsigned NOT NULL COMMENT '주문 ID',
  `address` text NOT NULL COMMENT '배달 주소',
  `address_detail` text COMMENT '배달 상세 주소',
  `latitude` decimal(10,8) DEFAULT NULL COMMENT '위도',
  `longitude` decimal(11,8) DEFAULT NULL COMMENT '경도',
  `to_owner` varchar(50) NOT NULL COMMENT '사장님 전달 메시지',
  `to_rider` varchar(50) NOT NULL COMMENT '라이더 전달 메시지',
  `delivery_tip` int unsigned NOT NULL COMMENT '배달비',
  `provide_cutlery` tinyint(1) NOT NULL DEFAULT '0' COMMENT '수저, 포크 수령 여부',
  `dispatched_at` timestamp NULL DEFAULT NULL COMMENT '배달 출발 일시',
  `completed_at` timestamp NULL DEFAULT NULL COMMENT '배달 완료 일시',
  `estimated_arrival_at` timestamp NULL DEFAULT NULL COMMENT '배달 완료 예상 일시',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (`order_id`),
  CONSTRAINT `fk_order_delivery_order_v2` FOREIGN KEY (`order_id`) REFERENCES `order_v2` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `order_menu` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '주문 메뉴 ID',
  `menu_name` varchar(255) NOT NULL COMMENT '메뉴 이름',
  `menu_option_name` varchar(255) DEFAULT NULL COMMENT '메뉴 옵션 이름',
  `menu_price` int unsigned NOT NULL COMMENT '메뉴 금액',
  `quantity` int unsigned NOT NULL COMMENT '수량',
  `order_id` varchar(64) NOT NULL COMMENT '주문 ID',
  `menu_price_name` varchar(255) DEFAULT NULL COMMENT '메뉴 가격 이름',
  PRIMARY KEY (`id`),
  KEY `fk_order_menu_order` (`order_id`),
  CONSTRAINT `fk_order_menu_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `order_menu_option` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '메뉴 옵션 ID',
  `option_name` varchar(255) NOT NULL COMMENT '옵션 이름',
  `option_price` int unsigned NOT NULL COMMENT '옵션 가격',
  `quantity` int unsigned NOT NULL COMMENT '옵션 수량',
  `order_menu_id` int unsigned NOT NULL COMMENT '주문 메뉴 ID',
  `option_group_name` varchar(255) NOT NULL COMMENT '주문 메뉴 옵션 이름',
  PRIMARY KEY (`id`),
  KEY `fk_order_menu_option_menu` (`order_menu_id`),
  CONSTRAINT `fk_order_menu_option_menu` FOREIGN KEY (`order_menu_id`) REFERENCES `order_menu` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `order_menu_option_v2` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '메뉴 옵션 ID',
  `order_menu_id` int unsigned NOT NULL COMMENT '주문 상점 메뉴 ID',
  `orderable_shop_menu_option_group_id` int unsigned NOT NULL COMMENT '주문 가능 상점 메뉴 옵션 그룹 ID',
  `orderable_shop_menu_option_id` int unsigned NOT NULL COMMENT '주문 가능 상점 메뉴 옵션 ID',
  `group_name` varchar(255) NOT NULL COMMENT '그룹 이름',
  `name` varchar(255) NOT NULL COMMENT '옵션 이름',
  `price` int unsigned NOT NULL COMMENT '옵션 가격',
  `quantity` int unsigned NOT NULL COMMENT '옵션 수량',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (`id`),
  KEY `fk_order_menu_option_menu_v2` (`order_menu_id`),
  CONSTRAINT `fk_order_menu_option_menu_v2` FOREIGN KEY (`order_menu_id`) REFERENCES `order_menu_v2` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `order_menu_v2` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '주문 메뉴 ID',
  `order_id` int unsigned NOT NULL COMMENT '주문 ID',
  `orderable_shop_menu_id` int unsigned NOT NULL COMMENT '주문 가능 상점 메뉴 ID',
  `orderable_shop_menu_price_id` int unsigned NOT NULL COMMENT '주문 가능 상점 메뉴 가격 ID',
  `name` varchar(255) NOT NULL COMMENT '메뉴 이름',
  `price_name` varchar(255) DEFAULT NULL COMMENT '가격 이름',
  `price` int unsigned NOT NULL COMMENT '가격',
  `quantity` int unsigned NOT NULL COMMENT '수량',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (`id`),
  KEY `fk_order_menu_order_v2` (`order_id`),
  CONSTRAINT `fk_order_menu_order_v2` FOREIGN KEY (`order_id`) REFERENCES `order_v2` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `order_takeout` (
  `order_id` varchar(64) NOT NULL COMMENT '주문 ID',
  `to_owner` varchar(50) DEFAULT NULL COMMENT '사장님 전달 메시지',
  `provide_cutlery` tinyint(1) NOT NULL DEFAULT '0' COMMENT '수저, 포크 수령 여부',
  PRIMARY KEY (`order_id`),
  CONSTRAINT `fk_order_pack_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `order_takeout_v2` (
  `order_id` int unsigned NOT NULL COMMENT '주문 ID',
  `to_owner` varchar(50) NOT NULL COMMENT '사장님 전달 메시지',
  `provide_cutlery` tinyint(1) NOT NULL DEFAULT '0' COMMENT '수저, 포크 수령 여부',
  `packaged_at` timestamp NULL DEFAULT NULL COMMENT '표장 완료 일시',
  `estimated_packaged_at` timestamp NULL DEFAULT NULL COMMENT '표장 완료 예상 일시',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (`order_id`),
  CONSTRAINT `fk_order_takeout_order_v2` FOREIGN KEY (`order_id`) REFERENCES `order_v2` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `order_v2` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '주문 고유 ID',
  `pg_order_id` varchar(64) NOT NULL COMMENT 'PG 주문 ID',
  `user_id` int unsigned NOT NULL COMMENT '유저 고유 ID',
  `phone_number` varchar(20) NOT NULL COMMENT '전화번호',
  `orderable_shop_id` int unsigned NOT NULL COMMENT '주문 가능 상점 ID',
  `orderable_shop_name` varchar(255) NOT NULL COMMENT '주문 가능 상점 이름',
  `orderable_shop_address` text NOT NULL COMMENT '주문 가능 상점 주소',
  `orderable_shop_address_detail` text COMMENT '주문 가능 상점 상세 주소',
  `total_product_price` int unsigned NOT NULL COMMENT '상품 총 금액',
  `discount_amount` int unsigned NOT NULL DEFAULT '0' COMMENT '할인 금액',
  `total_price` int unsigned NOT NULL COMMENT '주문 총 금액',
  `order_type` varchar(10) NOT NULL COMMENT '주문 타입',
  `status` varchar(10) NOT NULL COMMENT '주문 상태',
  `canceled_at` timestamp NULL DEFAULT NULL COMMENT '취소 일시',
  `canceled_reason` varchar(200) DEFAULT NULL COMMENT '취소 사유',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `orderable_shop` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `shop_id` int unsigned NOT NULL COMMENT '상점 ID',
  `delivery` tinyint(1) NOT NULL DEFAULT '1' COMMENT '배달 가능 여부',
  `takeout` tinyint(1) NOT NULL DEFAULT '1' COMMENT '포장 가능 여부',
  `service_event` tinyint(1) NOT NULL DEFAULT '0' COMMENT '서비스 증정 여부',
  `minimum_order_amount` int unsigned NOT NULL COMMENT '최소 주문 금액',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  UNIQUE KEY `shop_id` (`shop_id`),
  KEY `idx_orderable_shop_shop_id` (`shop_id`),
  KEY `idx_orderable_shop_minimum_order_amount` (`minimum_order_amount`,`shop_id`),
  KEY `idx_orderable_shop_filter_01` (`delivery`,`takeout`,`shop_id`),
  KEY `idx_orderable_shop_filter_02` (`minimum_order_amount`,`delivery`,`takeout`,`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `orderable_shop_delivery_option` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `orderable_shop_id` int unsigned NOT NULL COMMENT '주문 가능 상점 ID',
  `campus_delivery` tinyint(1) NOT NULL DEFAULT '1' COMMENT '교내 배달 가능 여부',
  `off_campus_delivery` tinyint(1) NOT NULL DEFAULT '1' COMMENT '교외 배달 가능 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `orderable_shop_image` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `orderable_shop_id` int unsigned NOT NULL COMMENT '주문 가능 상점 ID',
  `image_url` varchar(255) NOT NULL COMMENT '이미지 URL',
  `is_thumbnail` tinyint(1) NOT NULL DEFAULT '0' COMMENT '대표 이미지 여부',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  KEY `idx_images_on_orderable_shop_id` (`orderable_shop_id`),
  KEY `idx_thumbnail_images` (`orderable_shop_id`,`is_thumbnail`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `orderable_shop_menu` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `orderable_shop_id` int unsigned NOT NULL COMMENT '주문 가능한 상점 ID',
  `name` varchar(255) NOT NULL COMMENT '메뉴 이름',
  `description` text COMMENT '메뉴 설명',
  `is_sold_out` tinyint(1) NOT NULL DEFAULT '0' COMMENT '품절 여부',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  KEY `idx_orderable_shop_menu_orderable_shop_id` (`orderable_shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `orderable_shop_menu_group` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `orderable_shop_id` int unsigned NOT NULL COMMENT '주문 가능 상점 ID',
  `name` varchar(255) NOT NULL COMMENT '메뉴 그룹 이름',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  KEY `idx_orderable_shop_id` (`orderable_shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `orderable_shop_menu_group_map` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `orderable_shop_menu_group_id` int unsigned NOT NULL COMMENT '주문 가능 메뉴 그룹 ID',
  `orderable_shop_menu_id` int unsigned NOT NULL COMMENT '주문 가능 메뉴 ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  KEY `idx_map_on_menu_group_id` (`orderable_shop_menu_group_id`),
  KEY `idx_map_on_menu_id` (`orderable_shop_menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `orderable_shop_menu_images` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `orderable_shop_menu_id` int unsigned NOT NULL COMMENT '주문 가능 메뉴 ID',
  `image_url` varchar(255) NOT NULL COMMENT '이미지 URL',
  `is_thumbnail` tinyint(1) NOT NULL DEFAULT '0' COMMENT '대표 이미지 여부',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  KEY `idx_images_on_menu_id` (`orderable_shop_menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `orderable_shop_menu_option` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `orderable_shop_menu_option_group_id` int unsigned NOT NULL COMMENT '옵션 그룹 ID',
  `name` varchar(255) NOT NULL COMMENT '옵션 이름 (ex: 순한맛, 콜라)',
  `price` int unsigned NOT NULL COMMENT '추가 가격',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  KEY `idx_option_on_option_group_id` (`orderable_shop_menu_option_group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `orderable_shop_menu_option_group` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `orderable_shop_id` int unsigned NOT NULL COMMENT '주문 가능한 상점 ID',
  `name` varchar(255) NOT NULL COMMENT '옵션 그룹 이름 (ex: 맛, 음료 등)',
  `description` text COMMENT '옵션 그룹 설명',
  `is_required` tinyint(1) NOT NULL DEFAULT '0' COMMENT '필수 옵션 여부',
  `min_select` int unsigned NOT NULL DEFAULT '0' COMMENT '최소 선택 개수',
  `max_select` int unsigned DEFAULT NULL COMMENT '최대 선택 개수',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  KEY `idx_orderable_shop_id` (`orderable_shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `orderable_shop_menu_option_group_map` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `orderable_shop_menu_option_group_id` int unsigned NOT NULL COMMENT '옵션 그룹 ID',
  `orderable_shop_menu_id` int unsigned NOT NULL COMMENT '메뉴 ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  KEY `idx_map_on_option_group_id` (`orderable_shop_menu_option_group_id`),
  KEY `idx_map_on_menu_id` (`orderable_shop_menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `orderable_shop_menu_price` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `orderable_shop_menu_id` int unsigned NOT NULL COMMENT '메뉴 ID',
  `name` varchar(255) DEFAULT NULL COMMENT '가격 옵션 이름 (ex: 대, 중, 소)',
  `price` int unsigned NOT NULL COMMENT '가격',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  KEY `idx_price_on_menu_id` (`orderable_shop_menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `organizations` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '단체 계정 user_id',
  `name` varchar(100) NOT NULL COMMENT '단체명 (예: 총학생회, 컴퓨터공학부)',
  `location` varchar(255) NOT NULL COMMENT '방문 장소 (예: 학생회관 320호 총학생회 사무실)',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_organizations_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='단체 정보';

CREATE TABLE `owner_attachments` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `owner_id` int unsigned NOT NULL,
  `url` text CHARACTER SET utf8mb3 COLLATE utf8_bin NOT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `owner_shop_attachment_fk_owner_id` (`owner_id`),
  CONSTRAINT `owner_shop_attachment_fk_owner_id` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

CREATE TABLE `owners` (
  `user_id` int NOT NULL COMMENT 'user 고유 id',
  `company_registration_number` varchar(12) CHARACTER SET utf8mb3 COLLATE utf8_general_ci NOT NULL,
  `grant_shop` tinyint DEFAULT '0' COMMENT '상점 수정 권한',
  `grant_event` tinyint DEFAULT '0' COMMENT '이벤트 수정 권한',
  `account` varchar(11) CHARACTER SET utf8mb3 COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `company_registration_number_UNIQUE` (`company_registration_number`),
  UNIQUE KEY `account_UNIQUE` (`account`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

CREATE TABLE `password_resets` (
  `email` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci NOT NULL,
  `token` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci NOT NULL,
  `created_at` timestamp NULL DEFAULT NULL,
  KEY `password_resets_email_index` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_unicode_ci;

CREATE TABLE `payment` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `payment_key` varchar(200) NOT NULL COMMENT '결제 키',
  `amount` int unsigned NOT NULL COMMENT '결제 금액',
  `status` varchar(30) NOT NULL COMMENT '결제 상태',
  `method` varchar(30) NOT NULL COMMENT '결제 수단',
  `requested_at` timestamp NOT NULL COMMENT '결제 요청 일시',
  `approved_at` timestamp NOT NULL COMMENT '결제 승인 일시',
  `order_id` varchar(64) NOT NULL COMMENT '주문 번호',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_payment_key` (`id`),
  KEY `fk_payment_order` (`order_id`),
  CONSTRAINT `fk_payment_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `payment_cancel` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '결제 취소 ID',
  `transaction_key` varchar(64) NOT NULL COMMENT '취소 트랜잭션 키',
  `cancel_reason` varchar(200) NOT NULL COMMENT '취소 사유',
  `cancel_amount` int unsigned NOT NULL COMMENT '취소 금액',
  `canceled_at` timestamp NOT NULL COMMENT '취소 일시',
  `payment_id` int unsigned NOT NULL COMMENT '결제 ID',
  PRIMARY KEY (`id`),
  KEY `fk_payment_cancel_payment` (`payment_id`),
  CONSTRAINT `fk_payment_cancel_payment` FOREIGN KEY (`payment_id`) REFERENCES `payment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `payment_cancel_v2` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '결제 취소 ID',
  `payment_id` int unsigned NOT NULL COMMENT '결제 ID',
  `transaction_key` varchar(64) NOT NULL COMMENT '취소 트랜잭션 키',
  `reason` varchar(200) NOT NULL COMMENT '취소 사유',
  `amount` int unsigned NOT NULL COMMENT '취소 금액',
  `canceled_at` timestamp NOT NULL COMMENT '취소 일시',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (`id`),
  KEY `fk_payment_cancel_payment_v2` (`payment_id`),
  CONSTRAINT `fk_payment_cancel_payment_v2` FOREIGN KEY (`payment_id`) REFERENCES `payment_v2` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `payment_idempotency_key` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '결제 멱등키 ID',
  `user_id` int unsigned NOT NULL COMMENT '유저 ID',
  `idempotency_key` varchar(300) NOT NULL COMMENT '결제 멱등키',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_idempotency_key_user_id` (`user_id`),
  CONSTRAINT `fk_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `payment_v2` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `order_id` int unsigned NOT NULL COMMENT '주문 번호',
  `payment_key` varchar(200) NOT NULL COMMENT '결제 키',
  `amount` int unsigned NOT NULL COMMENT '결제 금액',
  `status` varchar(30) NOT NULL COMMENT '결제 상태',
  `method` varchar(30) NOT NULL COMMENT '결제 수단',
  `description` varchar(255) NOT NULL COMMENT '결제 설명',
  `easy_pay_company` varchar(255) DEFAULT NULL COMMENT '간편 결제사',
  `requested_at` timestamp NOT NULL COMMENT '결제 요청 일시',
  `approved_at` timestamp NOT NULL COMMENT '결제 승인 일시',
  `receipt` text NOT NULL COMMENT '영수증',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uq_payment_key` (`id`),
  KEY `fk_payment_order_v2` (`order_id`),
  CONSTRAINT `fk_payment_order_v2` FOREIGN KEY (`order_id`) REFERENCES `order_v2` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `rider_message` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '배달 기사 요청 사항 고유 ID',
  `content` varchar(100) NOT NULL COMMENT '배달 기사 요청 사항',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `search_articles` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'search articles 고유 id',
  `table_id` int unsigned NOT NULL COMMENT '게시판(table) 고유 id',
  `article_id` int unsigned NOT NULL COMMENT 'article(게시글) 고유 id',
  `title` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_bin NOT NULL COMMENT '게시글 제목',
  `content` text CHARACTER SET utf8mb3 COLLATE utf8_bin COMMENT '게시글 내용',
  `user_id` int unsigned DEFAULT NULL COMMENT 'user 고유 id',
  `nickname` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8_bin NOT NULL COMMENT '닉네임',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`),
  UNIQUE KEY `pk` (`id`),
  UNIQUE KEY `idx_unique` (`table_id`,`article_id`),
  KEY `idx_timestamp` (`created_at`),
  KEY `idx_is_deleted` (`is_deleted`),
  KEY `idx_nickname` (`nickname`,`is_deleted`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

CREATE TABLE `semester` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `semester` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8_bin NOT NULL COMMENT '학기',
  `year` int unsigned NOT NULL,
  `term` varchar(20) CHARACTER SET utf8mb3 COLLATE utf8_bin NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `semester_UNIQUE` (`semester`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

CREATE TABLE `shop_base_delivery_tip` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `shop_id` int unsigned NOT NULL COMMENT '상점 ID',
  `order_amount` int NOT NULL COMMENT '주문 금액 기준',
  `fee` int NOT NULL COMMENT '배달비',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  KEY `idx_shop_base_delivery_tip_shop_id` (`shop_id`),
  KEY `idx_shop_base_delivery_tip_01` (`shop_id`,`order_amount`,`fee`),
  KEY `idx_shop_base_delivery_tip_02` (`shop_id`,`is_deleted`,`fee`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `shop_benefit_categories` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(60) NOT NULL,
  `detail` varchar(255) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `on_image_url` varchar(255) NOT NULL,
  `off_image_url` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `shop_benefit_category_map` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `shop_id` int unsigned NOT NULL,
  `benefit_id` int unsigned NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `detail` varchar(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `shop_id` (`shop_id`),
  KEY `benefit_id` (`benefit_id`),
  CONSTRAINT `shop_benefit_category_map_ibfk_1` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`),
  CONSTRAINT `shop_benefit_category_map_ibfk_2` FOREIGN KEY (`benefit_id`) REFERENCES `shop_benefit_categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `shop_categories` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'shop_categories 고유 id',
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_bin NOT NULL COMMENT '카테고리 이름',
  `image_url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_bin DEFAULT NULL COMMENT '이미지 URL',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  `parent_category_id` int unsigned DEFAULT NULL COMMENT '상위 카테고리 id',
  `event_banner_image_url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_bin DEFAULT NULL COMMENT '이벤트 배너 이미지',
  `order_index` int NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_SHOP_CATEGORIES_ON_SHOP_PARENT_CATEGORIES` (`parent_category_id`),
  CONSTRAINT `FK_SHOP_CATEGORIES_ON_SHOP_PARENT_CATEGORIES` FOREIGN KEY (`parent_category_id`) REFERENCES `shop_parent_categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

CREATE TABLE `shop_category_map` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'shop_category_map 고유 id',
  `shop_id` int unsigned NOT NULL COMMENT 'shops 고유 id',
  `shop_category_id` int unsigned NOT NULL COMMENT 'shop_categories 고유 id',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`),
  UNIQUE KEY `SHOP_ID_AND_SHOP_CATEGORY_ID` (`shop_id`,`shop_category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

CREATE TABLE `shop_images` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'shop_images 고유 id',
  `shop_id` int unsigned NOT NULL COMMENT 'shops 고유 id',
  `image_url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_bin DEFAULT NULL COMMENT '이미지 URL',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`),
  UNIQUE KEY `SHOP_ID_AND_IMAGE_URL` (`shop_id`,`image_url`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

CREATE TABLE `shop_menu_categories` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'shop_menu_categories 고유 id',
  `shop_id` int unsigned NOT NULL COMMENT 'shops 고유 id',
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_bin NOT NULL COMMENT '카테고리 이름',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

CREATE TABLE `shop_menu_category_map` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'shop_menu_category_map 고유 id',
  `shop_menu_id` int unsigned NOT NULL COMMENT 'shop_menus 고유 id',
  `shop_menu_category_id` int unsigned NOT NULL COMMENT 'shop_menu_categories 고유 id',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`),
  UNIQUE KEY `SHOP_MENU_ID_AND_SHOP_MENU_CATEGORY_ID` (`shop_menu_id`,`shop_menu_category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

CREATE TABLE `shop_menu_details` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'shop_menu_details 고유 id',
  `shop_menu_id` int unsigned NOT NULL COMMENT 'shop_menus 고유 id',
  `option` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_bin DEFAULT NULL COMMENT '옵션 이름',
  `price` int unsigned NOT NULL COMMENT '가격',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`),
  UNIQUE KEY `SHOP_MENU_ID_AND_OPTION_AND_PRICE` (`shop_menu_id`,`option`,`price`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

CREATE TABLE `shop_menu_images` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'shop_menu_images 고유 id',
  `shop_menu_id` int unsigned NOT NULL COMMENT 'shop_menus 고유 id',
  `image_url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_bin NOT NULL COMMENT '이미지 URL',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`),
  UNIQUE KEY `SHOP_MENU_ID_AND_IMAGE_URL` (`shop_menu_id`,`image_url`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

CREATE TABLE `shop_menu_origin` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `shop_id` int unsigned NOT NULL COMMENT '상점 ID',
  `ingredient` varchar(50) DEFAULT NULL,
  `origin` varchar(50) DEFAULT NULL,
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  KEY `idx_shop_menu_origin_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `shop_menu_search_keywords` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `keyword` varchar(255) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `shop_menus` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'shop_menus 고유 id',
  `shop_id` int unsigned NOT NULL COMMENT 'shop 고유 id',
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci NOT NULL COMMENT '메뉴 이름',
  `description` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '메뉴 구성',
  `is_hidden` tinyint(1) NOT NULL DEFAULT '0' COMMENT '숨김 여부',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_unicode_ci;

CREATE TABLE `shop_notification_messages` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'shop_notification_messages 고유 id',
  `title` varchar(255) NOT NULL COMMENT '메세지 제목',
  `content` varchar(255) NOT NULL COMMENT '메세지 내용',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `shop_opens` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'shop_open 고유 id',
  `shop_id` int unsigned NOT NULL COMMENT 'shops 고유 id',
  `day_of_week` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8_bin NOT NULL COMMENT '요일',
  `closed` tinyint(1) NOT NULL COMMENT '휴무 여부',
  `open_time` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8_bin DEFAULT NULL COMMENT '오픈 시간',
  `close_time` varchar(10) CHARACTER SET utf8mb3 COLLATE utf8_bin DEFAULT NULL COMMENT '마감 시간',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`),
  KEY `shop_opens_filter` (`day_of_week`,`shop_id`,`open_time`,`close_time`),
  KEY `shop_opens_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

CREATE TABLE `shop_operation` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `shop_id` int unsigned NOT NULL COMMENT '상점 ID',
  `is_open` tinyint(1) NOT NULL DEFAULT '0' COMMENT '현재 상점 오픈 여부',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  UNIQUE KEY `shop_id` (`shop_id`),
  KEY `idx_shop_operation_shop_id` (`shop_id`),
  KEY `idx_shop_operation_shop_id_is_open` (`shop_id`,`is_open`),
  KEY `idx_shop_operation_shop_id_open_deleted` (`shop_id`,`is_open`,`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `shop_order_service_requests` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `shop_id` int unsigned NOT NULL COMMENT '식당 ID',
  `minimum_order_amount` int unsigned NOT NULL COMMENT '최소 주문 금액',
  `is_takeout` tinyint(1) NOT NULL DEFAULT '0' COMMENT '포장 여부',
  `delivery_option` varchar(50) NOT NULL COMMENT '배달 옵션',
  `campus_delivery_tip` int unsigned NOT NULL DEFAULT '0' COMMENT '캠퍼스 내 배달 팁',
  `off_campus_delivery_tip` int unsigned NOT NULL DEFAULT '0' COMMENT '캠퍼스 외 배달 팁',
  `business_license_url` varchar(255) NOT NULL COMMENT '사업자 등록증 URL',
  `business_certificate_url` varchar(255) NOT NULL COMMENT '영업 신고증 URL',
  `bank_copy_url` varchar(255) NOT NULL COMMENT '통장 사본 URL',
  `bank` varchar(10) NOT NULL COMMENT '은행명',
  `account_number` varchar(20) NOT NULL COMMENT '계좌 번호',
  `request_status` varchar(50) NOT NULL DEFAULT 'PENDING' COMMENT '요청 상태',
  `approved_at` timestamp NULL DEFAULT NULL COMMENT '승인 일자',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`),
  KEY `fk_shop_order_service_requests_shop_id` (`shop_id`),
  CONSTRAINT `fk_shop_order_service_requests_shop_id` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `shop_parent_categories` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'shop_parent_categories 고유 id',
  `name` varchar(255) NOT NULL COMMENT '메인 카테고리 이름',
  `notification_message_id` int unsigned NOT NULL COMMENT '알림 메시지 id',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  KEY `FK_MAIN_CATEGORIES_ON_SHOP_NOTIFICATION_MESSAGES` (`notification_message_id`),
  CONSTRAINT `FK_MAIN_CATEGORIES_ON_SHOP_NOTIFICATION_MESSAGES` FOREIGN KEY (`notification_message_id`) REFERENCES `shop_notification_messages` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `shop_review_images` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `review_id` int unsigned NOT NULL,
  `image_urls` text NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `shop_review_images_ibfk_1` (`review_id`),
  CONSTRAINT `shop_review_images_ibfk_1` FOREIGN KEY (`review_id`) REFERENCES `shop_reviews` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `shop_review_menus` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `review_id` int unsigned NOT NULL,
  `menu_name` text NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `shop_review_menus_ibfk_1` (`review_id`),
  CONSTRAINT `shop_review_menus_ibfk_1` FOREIGN KEY (`review_id`) REFERENCES `shop_reviews` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `shop_review_reports` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `review_id` int unsigned NOT NULL,
  `title` varchar(50) NOT NULL,
  `content` text,
  `user_id` int unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `status` varchar(25) DEFAULT 'UNHANDLED',
  PRIMARY KEY (`id`),
  KEY `shop_review_reports_ibfk_1` (`review_id`),
  KEY `shop_review_reports_ibfk_2` (`user_id`),
  CONSTRAINT `shop_review_reports_ibfk_1` FOREIGN KEY (`review_id`) REFERENCES `shop_reviews` (`id`) ON DELETE CASCADE,
  CONSTRAINT `shop_review_reports_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `shop_review_reports_categories` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `detail` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `shop_reviews` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `shop_id` int unsigned NOT NULL,
  `content` text NOT NULL,
  `rating` int NOT NULL,
  `reviewer_id` int unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `is_deleted` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `shop_reviews_ibfk_1` (`reviewer_id`),
  KEY `shop_reviews_ibfk_2` (`shop_id`),
  KEY `shop_reviews_rating` (`shop_id`,`rating`),
  KEY `idx_shop_reviews_covering` (`shop_id`,`is_deleted`,`rating`),
  CONSTRAINT `shop_reviews_ibfk_1` FOREIGN KEY (`reviewer_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `shop_reviews_ibfk_2` FOREIGN KEY (`shop_id`) REFERENCES `shops` (`id`) ON DELETE CASCADE,
  CONSTRAINT `shop_reviews_chk_1` CHECK ((`rating` between 0 and 5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `shops` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'shops 고유 id',
  `owner_id` int DEFAULT NULL COMMENT 'owner 고유 id',
  `name` varchar(50) NOT NULL COMMENT '가게 이름',
  `internal_name` varchar(50) NOT NULL COMMENT '가게 이름을 소문자로 변경하고 띄어쓰기 제거',
  `chosung` varchar(3) DEFAULT NULL COMMENT '가게 이름 앞자리 1글자의 초성',
  `phone` varchar(50) DEFAULT NULL COMMENT '전화 번호',
  `address` text COMMENT '주소',
  `address_detail` text COMMENT '상세 주소',
  `description` text COMMENT '세부 사항',
  `introduction` text COMMENT '가게 소개',
  `notice` text COMMENT '가게 알림',
  `delivery` tinyint(1) NOT NULL DEFAULT '0' COMMENT '배달 가능 여부',
  `delivery_price` int unsigned NOT NULL DEFAULT '0' COMMENT '배달 금액',
  `pay_card` tinyint(1) NOT NULL DEFAULT '0' COMMENT '카드 가능 여부',
  `pay_bank` tinyint(1) NOT NULL DEFAULT '0' COMMENT '계좌이체 가능 여부',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  `is_event` tinyint(1) NOT NULL DEFAULT '0' COMMENT '이벤트 진행 여부',
  `remarks` text COMMENT '이벤트 상세내용 등 부가내용',
  `hit` int unsigned NOT NULL DEFAULT '0' COMMENT '조회수',
  `bank` varchar(10) DEFAULT NULL,
  `account_number` varchar(20) DEFAULT NULL,
  `main_category_id` int unsigned DEFAULT NULL COMMENT '메인 카테고리 id',
  PRIMARY KEY (`id`),
  KEY `ix_internalname` (`internal_name`),
  KEY `FK_SHOPS_ON_SHOP_CATEGORIES` (`main_category_id`),
  CONSTRAINT `FK_SHOPS_ON_SHOP_CATEGORIES` FOREIGN KEY (`main_category_id`) REFERENCES `shop_categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `standard_graduation_requirements` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 id',
  `year` varchar(20) NOT NULL COMMENT '년도',
  `major_id` int unsigned NOT NULL COMMENT '전공 id',
  `course_type_id` int unsigned DEFAULT NULL COMMENT '이수 구분 id',
  `required_grades` int NOT NULL COMMENT '기준 학점',
  `is_deleted` tinyint DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  KEY `course_type_id` (`course_type_id`),
  KEY `major_id` (`major_id`),
  CONSTRAINT `standard_graduation_requirements_ibfk_1` FOREIGN KEY (`course_type_id`) REFERENCES `course_type` (`id`),
  CONSTRAINT `standard_graduation_requirements_ibfk_2` FOREIGN KEY (`major_id`) REFERENCES `major` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `student_course_calculation` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 id',
  `user_id` int unsigned NOT NULL COMMENT '유저 id',
  `standard_graduation_requirements_id` int unsigned DEFAULT NULL COMMENT '기준 졸업 요건 id',
  `completed_grades` int NOT NULL DEFAULT '0' COMMENT '이수 학점',
  `is_deleted` tinyint DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_user_standard_graduation_requirements` (`user_id`,`standard_graduation_requirements_id`),
  KEY `standard_graduation_requirements_id` (`standard_graduation_requirements_id`),
  CONSTRAINT `student_course_calculation_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `student_course_calculation_ibfk_2` FOREIGN KEY (`standard_graduation_requirements_id`) REFERENCES `standard_graduation_requirements` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `students` (
  `user_id` int NOT NULL COMMENT 'user 고유 id',
  `anonymous_nickname` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_bin DEFAULT NULL COMMENT '익명 닉네임',
  `student_number` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_bin DEFAULT NULL COMMENT '학번',
  `identity` smallint DEFAULT NULL COMMENT '신원(0: 학생, 1: 대학원생)',
  `is_graduated` tinyint(1) DEFAULT NULL COMMENT '졸업 여부',
  `department_id` int unsigned DEFAULT NULL COMMENT '학과 id',
  `major_id` int unsigned DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `anonymous_nickname_UNIQUE` (`anonymous_nickname`),
  KEY `fk_student_department` (`department_id`),
  KEY `fk_student_major` (`major_id`),
  CONSTRAINT `fk_student_department` FOREIGN KEY (`department_id`) REFERENCES `department` (`id`),
  CONSTRAINT `fk_student_major` FOREIGN KEY (`major_id`) REFERENCES `major` (`id`) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

CREATE TABLE `tech_stacks` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'tech_stacks 고유 id',
  `image_url` text CHARACTER SET utf8mb3 COLLATE utf8_bin COMMENT '이미지 링크',
  `name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8_bin NOT NULL COMMENT '기술 스택 명',
  `description` varchar(100) CHARACTER SET utf8mb3 COLLATE utf8_bin DEFAULT NULL COMMENT '기술 스택 설명',
  `track_id` int unsigned NOT NULL COMMENT 'track 고유 id',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

CREATE TABLE `timetable_frame` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 id',
  `user_id` int unsigned NOT NULL COMMENT '유저 id',
  `semester_id` int unsigned NOT NULL COMMENT '학기 id',
  `name` varchar(255) NOT NULL COMMENT '시간표 이름',
  `is_main` tinyint(1) NOT NULL DEFAULT '0' COMMENT '메인 시간표 여부',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '시간표 삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  KEY `timetable_frame_INDEX` (`user_id`,`semester_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `timetable_lecture` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '고유 id',
  `class_title` varchar(255) DEFAULT NULL COMMENT '수업 이름',
  `class_time` text,
  `class_place` text,
  `professor` varchar(255) DEFAULT NULL COMMENT '교수',
  `grades` varchar(2) NOT NULL DEFAULT '0' COMMENT '학점',
  `memo` varchar(255) DEFAULT NULL COMMENT '메모',
  `is_deleted` tinyint(1) DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  `lectures_id` int unsigned DEFAULT NULL COMMENT '강의_id',
  `frame_id` int unsigned DEFAULT NULL COMMENT '시간표 프레임 id',
  `course_type_id` int unsigned DEFAULT NULL COMMENT '이수구분 id',
  `general_education_area_id` int unsigned DEFAULT NULL COMMENT '교양영역 id',
  PRIMARY KEY (`id`),
  KEY `FK_TIMETABLE_FRAME_ON_TIMETABLE_LECTURE` (`frame_id`),
  KEY `FK_COURSE_TYPE_ON_TIMETABLE_LECTURE` (`course_type_id`),
  KEY `FK_GENERAL_EDUCATION_AREA_ON_TIMETABLE_LECTURE` (`general_education_area_id`),
  CONSTRAINT `FK_COURSE_TYPE_ON_TIMETABLE_LECTURE` FOREIGN KEY (`course_type_id`) REFERENCES `course_type` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_GENERAL_EDUCATION_AREA_ON_TIMETABLE_LECTURE` FOREIGN KEY (`general_education_area_id`) REFERENCES `general_education_area` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK_TIMETABLE_FRAME_ON_TIMETABLE_LECTURE` FOREIGN KEY (`frame_id`) REFERENCES `timetable_frame` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `tracks` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'tracks 테이블 고유 id',
  `name` varchar(50) CHARACTER SET utf8mb3 COLLATE utf8_bin NOT NULL COMMENT '트랙명',
  `headcount` int unsigned NOT NULL DEFAULT '0' COMMENT '인원수',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_bin;

CREATE TABLE `user_delivery_address` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '배달 주소 고유 ID',
  `user_id` int unsigned NOT NULL COMMENT '사용자 ID',
  `address_type` varchar(20) NOT NULL COMMENT '주소 타입 (CAMPUS, OFF_CAMPUS)',
  `campus_delivery_address_id` int unsigned DEFAULT NULL COMMENT '교내 배달 주소 ID',
  `zip_number` varchar(10) DEFAULT NULL COMMENT '교외 배달 주소 우편 번호',
  `si_do` varchar(50) DEFAULT NULL COMMENT '시/도',
  `si_gun_gu` varchar(50) DEFAULT NULL COMMENT '시/군/구',
  `eup_myeon_dong` varchar(50) DEFAULT NULL COMMENT '읍/면/동',
  `road` varchar(50) DEFAULT NULL COMMENT '도로명',
  `building` varchar(50) DEFAULT NULL COMMENT '건물명',
  `address` text COMMENT '상세 정보를 제외한 기본 주소',
  `detail_address` varchar(100) DEFAULT NULL COMMENT '상세 주소',
  `full_address` varchar(255) DEFAULT NULL COMMENT '전체 주소 (도로명/지번)',
  `last_used_at` timestamp NULL DEFAULT NULL COMMENT '마지막 사용 시간',
  `usage_count` int unsigned DEFAULT '0' COMMENT '사용 횟수',
  `is_default` tinyint(1) DEFAULT '0',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
  PRIMARY KEY (`id`),
  KEY `idx_user_address` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `user_notification_status` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `user_id` int unsigned NOT NULL,
  `last_notified_article_id` int NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  CONSTRAINT `user_notification_status_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `users` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'users 테이블 고유 id',
  `password` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '비밀번호',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '닉네임',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '이름',
  `phone_number` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '휴대 전화 번호',
  `user_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '유저 타입(Students or Owners)',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '학교 email',
  `gender` int unsigned DEFAULT NULL COMMENT '성별',
  `is_authed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '인증 여부',
  `last_logged_at` timestamp NULL DEFAULT NULL COMMENT '최근 로그인 일자',
  `profile_image_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '프로필 이미지 s3 url',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '탈퇴 여부',
  `anonymous_nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '회원가입 일자(생성 일자)',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  `device_token` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `user_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `anonymous_nickname` (`anonymous_nickname`),
  UNIQUE KEY `anonymous_nickname_2` (`anonymous_nickname`),
  UNIQUE KEY `nickname_UNIQUE` (`nickname`),
  UNIQUE KEY `uq_users_login_id` (`user_id`),
  UNIQUE KEY `uq_users_nickname` (`nickname`),
  UNIQUE KEY `uq_users_phone_number` (`phone_number`),
  UNIQUE KEY `uq_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `versions` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'versions 테이블 고유 id',
  `version` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci NOT NULL COMMENT '버전 명 (예시 : 1.1.0)',
  `type` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  `title` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '업데이트 문구 제목',
  `is_previous` tinyint(1) DEFAULT NULL,
  `content` text CHARACTER SET utf8mb3 COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `idx_versions_type_previous` (`type`,`is_previous`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb3 COLLATE=utf8_unicode_ci;

SET UNIQUE_CHECKS = 1;
SET FOREIGN_KEY_CHECKS = 1;
