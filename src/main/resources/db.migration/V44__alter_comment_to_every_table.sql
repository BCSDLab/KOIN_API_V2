ALTER TABLE `koin`.`versions`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'versions 테이블 고유 id' ,
    CHANGE COLUMN `version` `version` VARCHAR(255) NOT NULL COMMENT '버전 명 (예시 : 1.1.0)' ,
    CHANGE COLUMN `type` `type` VARCHAR(255) NOT NULL COMMENT '타입 (예시 : android)' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;

ALTER TABLE `koin`.`users_owners`
    DROP FOREIGN KEY `users_owners_fk_user_id`;
ALTER TABLE `koin`.`users_owners`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'users_owners 고유 id' ,
    CHANGE COLUMN `user_id` `user_id` INT UNSIGNED NOT NULL COMMENT 'users 중에 owners에 해당하는 user_id' ,
    CHANGE COLUMN `email` `email` VARCHAR(125) NULL DEFAULT NULL COMMENT '연락 가능한 이메일' ;
ALTER TABLE `koin`.`users_owners`
    ADD CONSTRAINT `users_owners_fk_user_id`
        FOREIGN KEY (`user_id`)
            REFERENCES `koin`.`users` (`id`)
            ON DELETE CASCADE;

ALTER TABLE `koin`.`users`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'users 테이블 고유 id' ,
    CHANGE COLUMN `account` `account` VARCHAR(50) NOT NULL COMMENT '계정 명' ,
    CHANGE COLUMN `password` `password` TEXT NOT NULL COMMENT '비밀번호' ,
    CHANGE COLUMN `nickname` `nickname` VARCHAR(50) NULL DEFAULT NULL COMMENT '닉네임' ,
    CHANGE COLUMN `name` `name` VARCHAR(50) NULL DEFAULT NULL COMMENT '이름' ,
    CHANGE COLUMN `phone_number` `phone_number` VARCHAR(255) NULL DEFAULT NULL COMMENT '휴대 전화 번호' ,
    CHANGE COLUMN `user_type` `user_type` VARCHAR(255) NOT NULL COMMENT '유저 타입(Students or Owners)' ,
    CHANGE COLUMN `gender` `gender` INT UNSIGNED NULL DEFAULT NULL COMMENT '성별' ,
    CHANGE COLUMN `is_authed` `is_authed` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '인증 여부' ,
    CHANGE COLUMN `last_logged_at` `last_logged_at` TIMESTAMP NULL DEFAULT NULL COMMENT '최근 로그인 일자' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '탈퇴 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '회원가입 일자(생성 일자)' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ,
    CHANGE COLUMN `profile_image_url` `profile_image_url` VARCHAR(255) NULL DEFAULT NULL COMMENT '프로필 이미지 s3 url' ;

ALTER TABLE `koin`.`tracks`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'tracks 테이블 고유 id' ,
    CHANGE COLUMN `name` `name` VARCHAR(50) NOT NULL COMMENT '트랙명' ,
    CHANGE COLUMN `headcount` `headcount` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '인원수' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;

ALTER TABLE `koin`.`timetables`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'timetables 고유 id' ,
    CHANGE COLUMN `user_id` `user_id` INT UNSIGNED NOT NULL COMMENT 'user 고유 id' ,
    CHANGE COLUMN `semester_id` `semester_id` INT UNSIGNED NOT NULL COMMENT '학기 고유 id' ,
    CHANGE COLUMN `code` `code` VARCHAR(10) NULL DEFAULT NULL COMMENT '과목 코드' ,
    CHANGE COLUMN `class_title` `class_title` VARCHAR(50) NOT NULL COMMENT '과목 명' ,
    CHANGE COLUMN `class_time` `class_time` VARCHAR(100) NOT NULL COMMENT '과목 시간' ,
    CHANGE COLUMN `class_place` `class_place` VARCHAR(30) NULL DEFAULT NULL COMMENT '수업 장소' ,
    CHANGE COLUMN `professor` `professor` VARCHAR(30) NULL DEFAULT NULL COMMENT '담당 교수' ,
    CHANGE COLUMN `grades` `grades` VARCHAR(2) NOT NULL COMMENT '학점' ,
    CHANGE COLUMN `lecture_class` `lecture_class` VARCHAR(3) NULL DEFAULT NULL COMMENT '분반' ,
    CHANGE COLUMN `target` `target` VARCHAR(200) NULL DEFAULT NULL COMMENT '학년 대상' ,
    CHANGE COLUMN `regular_number` `regular_number` VARCHAR(4) NULL DEFAULT NULL COMMENT '수강 정원' ,
    CHANGE COLUMN `design_score` `design_score` VARCHAR(4) NULL DEFAULT NULL COMMENT '설계' ,
    CHANGE COLUMN `department` `department` VARCHAR(30) NULL DEFAULT NULL COMMENT '학부' ,
    CHANGE COLUMN `memo` `memo` VARCHAR(200) NULL DEFAULT NULL COMMENT '사용자용 메모' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;

ALTER TABLE `koin`.`temp_comments`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'temp_comments 고유 id' ,
    CHANGE COLUMN `article_id` `article_id` INT UNSIGNED NOT NULL COMMENT 'article(게시글) 고유 id' ,
    CHANGE COLUMN `content` `content` TEXT NOT NULL COMMENT '내용' ,
    CHANGE COLUMN `nickname` `nickname` VARCHAR(50) NOT NULL COMMENT '작성자 닉네임' ,
    CHANGE COLUMN `password` `password` TEXT NOT NULL COMMENT '익명댓글 비밀번호' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;

ALTER TABLE `koin`.`temp_articles`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'temp_articles 고유 id' ,
    CHANGE COLUMN `title` `title` VARCHAR(255) NOT NULL COMMENT '제목' ,
    CHANGE COLUMN `content` `content` TEXT NULL DEFAULT NULL COMMENT '내용' ,
    CHANGE COLUMN `nickname` `nickname` VARCHAR(50) NOT NULL COMMENT '작성자 닉네임' ,
    CHANGE COLUMN `password` `password` TEXT NOT NULL COMMENT '익명게시글 비밀번호' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '작성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ,
    CHANGE COLUMN `hit` `hit` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '조회수' ,
    CHANGE COLUMN `comment_count` `comment_count` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '전체 댓글 수' ;

ALTER TABLE `koin`.`tech_stacks`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'tech_stacks 고유 id' ,
    CHANGE COLUMN `image_url` `image_url` TEXT NULL DEFAULT NULL COMMENT '이미지 링크' ,
    CHANGE COLUMN `name` `name` VARCHAR(50) NOT NULL COMMENT '기술 스택 명' ,
    CHANGE COLUMN `description` `description` VARCHAR(100) NULL DEFAULT NULL COMMENT '기술 스택 설명' ,
    CHANGE COLUMN `track_id` `track_id` INT UNSIGNED NOT NULL COMMENT 'track 고유 id' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;

ALTER TABLE `koin`.`students`
    CHANGE COLUMN `user_id` `user_id` INT NOT NULL COMMENT 'user 고유 id' ,
    CHANGE COLUMN `anonymous_nickname` `anonymous_nickname` VARCHAR(255) NULL DEFAULT NULL COMMENT '익명 닉네임' ,
    CHANGE COLUMN `student_number` `student_number` VARCHAR(255) NULL DEFAULT NULL COMMENT '학번' ,
    CHANGE COLUMN `major` `major` VARCHAR(50) NULL DEFAULT NULL COMMENT '전공' ,
    CHANGE COLUMN `identity` `identity` SMALLINT NULL DEFAULT NULL COMMENT '신원(0: 학생, 1: 대학원생)' ,
    CHANGE COLUMN `is_graduated` `is_graduated` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '졸업 여부' ,
    CHANGE COLUMN `auth_token` `auth_token` VARCHAR(255) NULL DEFAULT NULL COMMENT '이메일 인증 토큰' ,
    CHANGE COLUMN `auth_expired_at` `auth_expired_at` VARCHAR(255) NULL DEFAULT NULL COMMENT '이메일 인증 토큰 만료 시간' ,
    CHANGE COLUMN `reset_token` `reset_token` VARCHAR(255) NULL DEFAULT NULL COMMENT '비밀번호 초기화 토큰' ,
    CHANGE COLUMN `reset_expired_at` `reset_expired_at` VARCHAR(255) NULL DEFAULT NULL COMMENT '비밀번호 초기화 토큰 만료 시간' ;

ALTER TABLE `koin`.`shops`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'shops 고유 id' ,
    CHANGE COLUMN `owner_id` `owner_id` INT NULL DEFAULT NULL COMMENT 'owner 고유 id' ,
    CHANGE COLUMN `name` `name` VARCHAR(50) NOT NULL COMMENT '가게 이름' ,
    CHANGE COLUMN `internal_name` `internal_name` VARCHAR(50) NOT NULL COMMENT '가게 이름을 소문자로 변경하고 띄어쓰기 제거' ,
    CHANGE COLUMN `chosung` `chosung` VARCHAR(3) NULL DEFAULT NULL COMMENT '가게 이름 앞자리 1글자의 초성' ,
    CHANGE COLUMN `category` `category` VARCHAR(10) NOT NULL COMMENT '가게 카테고리' ,
    CHANGE COLUMN `phone` `phone` VARCHAR(50) NULL DEFAULT NULL COMMENT '전화 번호' ,
    CHANGE COLUMN `open_time` `open_time` VARCHAR(10) NULL DEFAULT NULL COMMENT '오픈 시간' ,
    CHANGE COLUMN `close_time` `close_time` VARCHAR(10) NULL DEFAULT NULL COMMENT '마감 시간' ,
    CHANGE COLUMN `weekend_open_time` `weekend_open_time` VARCHAR(10) NULL DEFAULT NULL COMMENT '주말 오픈 시간' ,
    CHANGE COLUMN `weekend_close_time` `weekend_close_time` VARCHAR(10) NULL DEFAULT NULL COMMENT '주말 마감 시간' ,
    CHANGE COLUMN `image_urls` `image_urls` TEXT NOT NULL COMMENT '이미지 링크' ,
    CHANGE COLUMN `address` `address` TEXT NULL DEFAULT NULL COMMENT '주소' ,
    CHANGE COLUMN `description` `description` TEXT NULL DEFAULT NULL COMMENT '세부 사항' ,
    CHANGE COLUMN `delivery` `delivery` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '배달 가능 여부' ,
    CHANGE COLUMN `delivery_price` `delivery_price` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '배달 금액' ,
    CHANGE COLUMN `pay_card` `pay_card` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '카드 가능 여부' ,
    CHANGE COLUMN `pay_bank` `pay_bank` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '계좌이체 가능 여부' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ,
    CHANGE COLUMN `is_event` `is_event` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '이벤트 진행 여부' ,
    CHANGE COLUMN `remarks` `remarks` TEXT NULL DEFAULT NULL COMMENT '이벤트 상세내용 등 부가내용' ,
    CHANGE COLUMN `hit` `hit` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '조회수' ;

ALTER TABLE `koin`.`shop_view_logs`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'shop_view_logs 고유 id' ,
    CHANGE COLUMN `shop_id` `shop_id` INT UNSIGNED NOT NULL COMMENT '가게 고유 id' ,
    CHANGE COLUMN `user_id` `user_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'user 고유 id' ,
    CHANGE COLUMN `expired_at` `expired_at` TIMESTAMP NULL DEFAULT NULL COMMENT '만료 시간' ,
    CHANGE COLUMN `ip` `ip` VARCHAR(45) NOT NULL COMMENT '조회한 IP 주소' ;

ALTER TABLE `koin`.`shop_menus`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'shop_menus 고유 id' ,
    CHANGE COLUMN `shop_id` `shop_id` INT UNSIGNED NOT NULL COMMENT 'shop 고유 id' ,
    CHANGE COLUMN `name` `name` VARCHAR(255) NOT NULL COMMENT '메뉴 이름' ,
    CHANGE COLUMN `price_type` `price_type` TEXT NOT NULL COMMENT '메뉴 사이즈에 따른 가격' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;

ALTER TABLE `koin`.`semester`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL COMMENT 'semester 고유 id' ,
    CHANGE COLUMN `semester` `semester` VARCHAR(10) NOT NULL COMMENT '학기' ;

ALTER TABLE `koin`.`search_articles`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'search articles 고유 id' ,
    CHANGE COLUMN `table_id` `table_id` INT UNSIGNED NOT NULL COMMENT '게시판(table) 고유 id' ,
    CHANGE COLUMN `article_id` `article_id` INT UNSIGNED NOT NULL COMMENT 'article(게시글) 고유 id' ,
    CHANGE COLUMN `title` `title` VARCHAR(255) NOT NULL COMMENT '게시글 제목' ,
    CHANGE COLUMN `content` `content` TEXT NULL DEFAULT NULL COMMENT '게시글 내용' ,
    CHANGE COLUMN `user_id` `user_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'user 고유 id' ,
    CHANGE COLUMN `nickname` `nickname` VARCHAR(50) NOT NULL COMMENT '닉네임' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;

ALTER TABLE `koin`.`owners`
    CHANGE COLUMN `user_id` `user_id` INT NOT NULL COMMENT 'user 고유 id' ,
    CHANGE COLUMN `company_registration_number` `company_registration_number` VARCHAR(12) NOT NULL COMMENT '사업자등록번호' ,
    CHANGE COLUMN `company_registration_certificate_image_url` `company_registration_certificate_image_url` VARCHAR(255) NOT NULL COMMENT '사업자 등록증 이미지 url' ,
    CHANGE COLUMN `grant_shop` `grant_shop` TINYINT NULL DEFAULT '0' COMMENT '상점 수정 권한' ,
    CHANGE COLUMN `grant_event` `grant_event` TINYINT NULL DEFAULT '0' COMMENT '이벤트 수정 권한' ;

ALTER TABLE `koin`.`notice_articles`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'notice articles 고유 id' ,
    CHANGE COLUMN `board_id` `board_id` INT UNSIGNED NOT NULL COMMENT '게시판 고유 id' ,
    CHANGE COLUMN `title` `title` VARCHAR(255) NOT NULL COMMENT '제목' ,
    CHANGE COLUMN `content` `content` MEDIUMTEXT NULL DEFAULT NULL COMMENT '내용' ,
    CHANGE COLUMN `author` `author` VARCHAR(50) NOT NULL COMMENT '작성자' ,
    CHANGE COLUMN `hit` `hit` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '조회수' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `article_num` `article_num` INT UNSIGNED NOT NULL COMMENT '게시물 번호' ,
    CHANGE COLUMN `permalink` `permalink` VARCHAR(100) NOT NULL COMMENT '기존 게시글 url' ,
    CHANGE COLUMN `has_notice` `has_notice` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '기존에 올라왔는지 여부' ,
    CHANGE COLUMN `registered_at` `registered_at` VARCHAR(255) NULL DEFAULT NULL COMMENT '등록 일자' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;

ALTER TABLE `koin`.`members`
    CHANGE COLUMN `id` `id` INT NOT NULL AUTO_INCREMENT COMMENT 'members 고유 id' ,
    CHANGE COLUMN `name` `name` VARCHAR(50) NOT NULL COMMENT '이름' ,
    CHANGE COLUMN `student_number` `student_number` VARCHAR(255) NULL DEFAULT NULL COMMENT '학번' ,
    CHANGE COLUMN `track_id` `track_id` INT UNSIGNED NOT NULL COMMENT '소속 트랙 고유 id' ,
    CHANGE COLUMN `position` `position` VARCHAR(255) NOT NULL COMMENT '직급' ,
    CHANGE COLUMN `email` `email` VARCHAR(100) NULL DEFAULT NULL COMMENT '이메일' ,
    CHANGE COLUMN `image_url` `image_url` TEXT NULL DEFAULT NULL COMMENT '이미지 링크' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;

ALTER TABLE `koin`.`lost_items`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'lost items 고유 id' ,
    CHANGE COLUMN `type` `type` INT UNSIGNED NOT NULL COMMENT '서비스 타입(0:습득 서비스 / 1:분실 서비스)' ,
    CHANGE COLUMN `title` `title` VARCHAR(255) NOT NULL COMMENT '제목' ,
    CHANGE COLUMN `location` `location` VARCHAR(255) NULL DEFAULT NULL COMMENT '분실물 위치' ,
    CHANGE COLUMN `content` `content` TEXT NULL DEFAULT NULL COMMENT '내용' ,
    CHANGE COLUMN `user_id` `user_id` INT UNSIGNED NOT NULL COMMENT '작성자 user 고유 id' ,
    CHANGE COLUMN `nickname` `nickname` VARCHAR(50) NOT NULL COMMENT '작성자 user 닉네임' ,
    CHANGE COLUMN `state` `state` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '상태정보(0: 찾는중 / 1:돌려받음)' ,
    CHANGE COLUMN `phone` `phone` VARCHAR(255) NULL DEFAULT NULL COMMENT '전화 번호' ,
    CHANGE COLUMN `is_phone_open` `is_phone_open` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '전화번호 공개 여부(0:비공개 / 1:공개)' ,
    CHANGE COLUMN `image_urls` `image_urls` TEXT NULL DEFAULT NULL COMMENT '이미지 링크' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ,
    CHANGE COLUMN `thumbnail` `thumbnail` VARCHAR(510) NULL DEFAULT NULL COMMENT '썸네일 이미지' ,
    CHANGE COLUMN `hit` `hit` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '조회수' ,
    CHANGE COLUMN `ip` `ip` VARCHAR(45) NOT NULL COMMENT 'IP 주소' ,
    CHANGE COLUMN `comment_count` `comment_count` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '전체 댓글 수' ,
    CHANGE COLUMN `date` `date` DATE NULL DEFAULT NULL COMMENT '분실 물품 날짜' ;

ALTER TABLE `koin`.`lost_item_view_logs`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'lost item view logs 고유 id' ,
    CHANGE COLUMN `lost_item_id` `lost_item_id` INT UNSIGNED NOT NULL COMMENT 'lost_item(분실물) 고유 id' ,
    CHANGE COLUMN `user_id` `user_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'user 고유 id' ,
    CHANGE COLUMN `expired_at` `expired_at` TIMESTAMP NULL DEFAULT NULL COMMENT '만료 시간' ,
    CHANGE COLUMN `ip` `ip` VARCHAR(45) NOT NULL COMMENT 'IP 주소' ;

ALTER TABLE `koin`.`lost_item_comments`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'lost item comments 고유 id' ,
    CHANGE COLUMN `lost_item_id` `lost_item_id` INT UNSIGNED NOT NULL COMMENT 'lost item (분실물) 고유 id' ,
    CHANGE COLUMN `content` `content` TEXT NOT NULL COMMENT '내용' ,
    CHANGE COLUMN `user_id` `user_id` INT UNSIGNED NOT NULL COMMENT '답글 user 고유 id' ,
    CHANGE COLUMN `nickname` `nickname` VARCHAR(50) NOT NULL COMMENT '답글 user의 닉네임' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;

ALTER TABLE `koin`.`lectures`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'lectures 고유 id' ,
    CHANGE COLUMN `semester_date` `semester_date` VARCHAR(6) NOT NULL COMMENT '학기' ,
    CHANGE COLUMN `code` `code` VARCHAR(10) NOT NULL COMMENT '강의 코드' ,
    CHANGE COLUMN `name` `name` VARCHAR(50) NOT NULL COMMENT '강의 이름' ,
    CHANGE COLUMN `grades` `grades` VARCHAR(2) NOT NULL COMMENT '대상 학년' ,
    CHANGE COLUMN `class` `class` VARCHAR(3) NOT NULL COMMENT '강의 분반' ,
    CHANGE COLUMN `regular_number` `regular_number` VARCHAR(4) NOT NULL COMMENT '수강 인원' ,
    CHANGE COLUMN `department` `department` VARCHAR(30) NOT NULL COMMENT '강의 학과' ,
    CHANGE COLUMN `target` `target` VARCHAR(200) NOT NULL COMMENT '강의 대상' ,
    CHANGE COLUMN `professor` `professor` VARCHAR(30) NULL DEFAULT NULL COMMENT '강의 교수' ,
    CHANGE COLUMN `is_english` `is_english` VARCHAR(2) NOT NULL COMMENT '영어강의 여부' ,
    CHANGE COLUMN `design_score` `design_score` VARCHAR(2) NOT NULL COMMENT '설계 학점' ,
    CHANGE COLUMN `is_elearning` `is_elearning` VARCHAR(2) NOT NULL COMMENT '이러닝 여부' ,
    CHANGE COLUMN `class_time` `class_time` VARCHAR(100) NOT NULL COMMENT '강의 시간' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;

ALTER TABLE `koin`.`lands`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'lands 고유 id' ,
    CHANGE COLUMN `name` `name` VARCHAR(255) NOT NULL COMMENT '건물 이름' ,
    CHANGE COLUMN `internal_name` `internal_name` VARCHAR(50) NOT NULL COMMENT '건물 이름 소문자 변환 및 띄어쓰기 제거' ,
    CHANGE COLUMN `size` `size` VARCHAR(255) NULL DEFAULT NULL COMMENT '방 크기' ,
    CHANGE COLUMN `room_type` `room_type` VARCHAR(255) NULL DEFAULT NULL COMMENT '원룸 종류' ,
    CHANGE COLUMN `latitude` `latitude` VARCHAR(255) NULL DEFAULT NULL COMMENT '위도' ,
    CHANGE COLUMN `longitude` `longitude` VARCHAR(255) NULL DEFAULT NULL COMMENT '경도' ,
    CHANGE COLUMN `phone` `phone` VARCHAR(255) NULL DEFAULT NULL COMMENT '전화번호' ,
    CHANGE COLUMN `image_urls` `image_urls` TEXT NULL DEFAULT NULL COMMENT '이미지 링크' ,
    CHANGE COLUMN `address` `address` TEXT NULL DEFAULT NULL COMMENT '주소' ,
    CHANGE COLUMN `description` `description` TEXT NULL DEFAULT NULL COMMENT '세부 사항' ,
    CHANGE COLUMN `floor` `floor` INT UNSIGNED NULL DEFAULT NULL COMMENT '층 수' ,
    CHANGE COLUMN `deposit` `deposit` VARCHAR(255) NULL DEFAULT NULL COMMENT '보증금' ,
    CHANGE COLUMN `monthly_fee` `monthly_fee` VARCHAR(255) NULL DEFAULT NULL COMMENT '월세' ,
    CHANGE COLUMN `charter_fee` `charter_fee` VARCHAR(255) NULL DEFAULT NULL COMMENT '전세' ,
    CHANGE COLUMN `management_fee` `management_fee` VARCHAR(255) NULL DEFAULT NULL COMMENT '관리비' ,
    CHANGE COLUMN `opt_refrigerator` `opt_refrigerator` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '냉장고 보유 여부' ,
    CHANGE COLUMN `opt_closet` `opt_closet` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '옷장 보유 여부' ,
    CHANGE COLUMN `opt_tv` `opt_tv` TINYINT(1) NOT NULL DEFAULT '0' COMMENT 'tv 보유 여부' ,
    CHANGE COLUMN `opt_microwave` `opt_microwave` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '전자레인지 보유 여부' ,
    CHANGE COLUMN `opt_gas_range` `opt_gas_range` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '가스레인지 보유 여부' ,
    CHANGE COLUMN `opt_induction` `opt_induction` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '인덕션 보유 여부' ,
    CHANGE COLUMN `opt_water_purifier` `opt_water_purifier` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '정수기 보유 여부' ,
    CHANGE COLUMN `opt_air_conditioner` `opt_air_conditioner` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '에어컨 보유 여부' ,
    CHANGE COLUMN `opt_washer` `opt_washer` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '샤워기 보유 여부' ,
    CHANGE COLUMN `opt_bed` `opt_bed` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '침대 보유 여부' ,
    CHANGE COLUMN `opt_desk` `opt_desk` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '책상 보유 여부' ,
    CHANGE COLUMN `opt_shoe_closet` `opt_shoe_closet` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '신발장 보유 여부' ,
    CHANGE COLUMN `opt_electronic_door_locks` `opt_electronic_door_locks` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '전자 도어락 보유 여부' ,
    CHANGE COLUMN `opt_bidet` `opt_bidet` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '비데 보유 여부' ,
    CHANGE COLUMN `opt_veranda` `opt_veranda` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '베란다 보유 여부' ,
    CHANGE COLUMN `opt_elevator` `opt_elevator` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '엘레베이터 보유 여부' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;

ALTER TABLE `koin`.`land_comments`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'land comments 고유 id' ,
    CHANGE COLUMN `user_id` `user_id` INT UNSIGNED NOT NULL COMMENT 'user 고유 id' ,
    CHANGE COLUMN `land_id` `land_id` INT UNSIGNED NOT NULL COMMENT 'land(원룸) 고유 id' ,
    CHANGE COLUMN `content` `content` TEXT NOT NULL COMMENT '내용' ,
    CHANGE COLUMN `score` `score` INT UNSIGNED NOT NULL COMMENT '평점' ,
    CHANGE COLUMN `nickname` `nickname` VARCHAR(50) NOT NULL COMMENT '댓글 user의 닉네임' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;

ALTER TABLE `koin`.`items`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'items 고유 id' ,
    CHANGE COLUMN `type` `type` INT UNSIGNED NOT NULL COMMENT '서비스 타입(0:팝니다 / 1:삽니다)' ,
    CHANGE COLUMN `title` `title` VARCHAR(255) NOT NULL COMMENT '제목' ,
    CHANGE COLUMN `content` `content` TEXT NULL DEFAULT NULL COMMENT '내용' ,
    CHANGE COLUMN `user_id` `user_id` INT UNSIGNED NOT NULL COMMENT '작성자 user 고유 id' ,
    CHANGE COLUMN `nickname` `nickname` VARCHAR(50) NOT NULL COMMENT '작성자 user 닉네임' ,
    CHANGE COLUMN `state` `state` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '상태 정보(0:판매중 / 1:판매완료 / 2:판매중지)' ,
    CHANGE COLUMN `price` `price` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '가격' ,
    CHANGE COLUMN `phone` `phone` VARCHAR(255) NULL DEFAULT NULL COMMENT '전화번호' ,
    CHANGE COLUMN `is_phone_open` `is_phone_open` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '전화번호 공개 여부(0:비공개 / 1:공개)' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ,
    CHANGE COLUMN `thumbnail` `thumbnail` VARCHAR(510) NULL DEFAULT NULL COMMENT '썸네일 이미지 링크' ,
    CHANGE COLUMN `hit` `hit` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '조회수' ,
    CHANGE COLUMN `ip` `ip` VARCHAR(45) NOT NULL COMMENT 'IP 주소' ;

ALTER TABLE `koin`.`item_view_logs`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'item view logs 고유 id' ,
    CHANGE COLUMN `item_id` `item_id` INT UNSIGNED NOT NULL COMMENT 'item 고유 id' ,
    CHANGE COLUMN `user_id` `user_id` INT UNSIGNED NULL DEFAULT NULL COMMENT 'user(본 사람) 고유 id' ,
    CHANGE COLUMN `expired_at` `expired_at` TIMESTAMP NULL DEFAULT NULL COMMENT '만료 시간' ,
    CHANGE COLUMN `ip` `ip` VARCHAR(45) NOT NULL COMMENT 'IP 주소' ;

ALTER TABLE `koin`.`item_comments`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'item comments 고유 id' ,
    CHANGE COLUMN `item_id` `item_id` INT UNSIGNED NOT NULL COMMENT 'item 고유 id' ,
    CHANGE COLUMN `content` `content` TEXT NOT NULL COMMENT '내용' ,
    CHANGE COLUMN `user_id` `user_id` INT UNSIGNED NOT NULL COMMENT '답글 user 고유 id' ,
    CHANGE COLUMN `nickname` `nickname` VARCHAR(50) NOT NULL COMMENT '답글 user 닉네임' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;

ALTER TABLE `koin`.`faqs`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'faqs 고유 id' ,
    CHANGE COLUMN `question` `question` VARCHAR(255) NOT NULL COMMENT '질문' ,
    CHANGE COLUMN `answer` `answer` TEXT NOT NULL COMMENT '답변' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ,
    CHANGE COLUMN `circle_id` `circle_id` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '관련 circle(동아리) id' ;

ALTER TABLE `koin`.`event_comments`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'event comments 고유 id' ,
    CHANGE COLUMN `article_id` `article_id` INT UNSIGNED NOT NULL COMMENT 'article(게시글) 고유 id' ,
    CHANGE COLUMN `content` `content` TEXT NOT NULL COMMENT '내용' ,
    CHANGE COLUMN `user_id` `user_id` INT UNSIGNED NOT NULL COMMENT '답글 user 고유 id' ,
    CHANGE COLUMN `nickname` `nickname` VARCHAR(50) NOT NULL COMMENT '답글 user 닉네임' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;

ALTER TABLE `koin`.`event_articles_view_logs`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'event articles view logs 고유 id' ,
    CHANGE COLUMN `event_articles_id` `event_articles_id` INT UNSIGNED NOT NULL COMMENT 'event articles 고유 id' ,
    CHANGE COLUMN `user_id` `user_id` INT UNSIGNED NULL DEFAULT NULL COMMENT '게시물을 본 user 고유 id' ,
    CHANGE COLUMN `expired_at` `expired_at` TIMESTAMP NULL DEFAULT NULL COMMENT '만료 일자' ,
    CHANGE COLUMN `ip` `ip` VARCHAR(45) NOT NULL COMMENT 'IP 주소' ;

ALTER TABLE `koin`.`event_articles`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'event articles 고유 id' ,
    CHANGE COLUMN `shop_id` `shop_id` INT UNSIGNED NOT NULL COMMENT 'Shop(가게) 고유 id' ,
    CHANGE COLUMN `title` `title` VARCHAR(255) NOT NULL COMMENT '제목' ,
    CHANGE COLUMN `event_title` `event_title` VARCHAR(50) NOT NULL COMMENT '홍보 문구' ,
    CHANGE COLUMN `content` `content` TEXT NOT NULL COMMENT '내용' ,
    CHANGE COLUMN `user_id` `user_id` INT NOT NULL COMMENT 'user(작성자) 고유 id' ,
    CHANGE COLUMN `nickname` `nickname` VARCHAR(50) NOT NULL COMMENT '작성자 user 닉네임' ,
    CHANGE COLUMN `thumbnail` `thumbnail` VARCHAR(255) NULL DEFAULT NULL COMMENT '썸네일 이미지' ,
    CHANGE COLUMN `hit` `hit` INT NOT NULL DEFAULT '0' COMMENT '조회수' ,
    CHANGE COLUMN `ip` `ip` VARCHAR(45) NOT NULL COMMENT 'IP 주소' ,
    CHANGE COLUMN `start_date` `start_date` DATE NOT NULL COMMENT '행사 시작일' ,
    CHANGE COLUMN `end_date` `end_date` DATE NOT NULL COMMENT '행사 마감일' ,
    CHANGE COLUMN `comment_count` `comment_count` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '전체 댓글수' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;

ALTER TABLE `koin`.`dining_menus`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'dining menus 고유 id' ,
    CHANGE COLUMN `date` `date` DATE NOT NULL COMMENT '일자' ,
    CHANGE COLUMN `type` `type` VARCHAR(9) NOT NULL COMMENT '식사 유형(아침 , 점심, 저녁)' ,
    CHANGE COLUMN `place` `place` VARCHAR(9) NOT NULL COMMENT '종류(양식, 한식..)' ,
    CHANGE COLUMN `price_card` `price_card` INT UNSIGNED NULL DEFAULT NULL COMMENT '카드 금액' ,
    CHANGE COLUMN `price_cash` `price_cash` INT UNSIGNED NULL DEFAULT NULL COMMENT '현금 금액' ,
    CHANGE COLUMN `kcal` `kcal` INT UNSIGNED NULL DEFAULT NULL COMMENT '칼로리' ,
    CHANGE COLUMN `menu` `menu` TEXT NOT NULL COMMENT '메뉴' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;

ALTER TABLE `koin`.`comments`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'comment 고유 id' ,
    CHANGE COLUMN `article_id` `article_id` INT UNSIGNED NOT NULL COMMENT '게시글 고유 id' ,
    CHANGE COLUMN `content` `content` TEXT NOT NULL COMMENT '내용' ,
    CHANGE COLUMN `user_id` `user_id` INT UNSIGNED NOT NULL COMMENT '답글 user 고유 id' ,
    CHANGE COLUMN `nickname` `nickname` VARCHAR(50) NOT NULL COMMENT '답글 user 닉네임' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;

ALTER TABLE `koin`.`circles`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'circles 고유 id' ,
    CHANGE COLUMN `category` `category` VARCHAR(10) NOT NULL COMMENT '동아리 카테고리' ,
    CHANGE COLUMN `name` `name` VARCHAR(50) NOT NULL COMMENT '동아리 이름' ,
    CHANGE COLUMN `line_description` `line_description` VARCHAR(255) NULL DEFAULT NULL COMMENT '한 줄 설명' ,
    CHANGE COLUMN `logo_url` `logo_url` TEXT NULL DEFAULT NULL COMMENT '로고 이미지 링크' ,
    CHANGE COLUMN `description` `description` TEXT NULL DEFAULT NULL COMMENT '세부 사항' ,
    CHANGE COLUMN `link_urls` `link_urls` TEXT NULL DEFAULT NULL COMMENT '외부 링크' ,
    CHANGE COLUMN `background_img_url` `background_img_url` TEXT NULL DEFAULT NULL COMMENT '배경 이미지 링크' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `professor` `professor` VARCHAR(255) NULL DEFAULT NULL COMMENT '담당 교수' ,
    CHANGE COLUMN `location` `location` VARCHAR(255) NULL DEFAULT NULL COMMENT '동아리방 위치' ,
    CHANGE COLUMN `major_business` `major_business` VARCHAR(255) NULL DEFAULT NULL COMMENT '주요 사업' ,
    CHANGE COLUMN `introduce_url` `introduce_url` VARCHAR(255) NULL DEFAULT NULL COMMENT '동아리 소개 홈페이지 url' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;

ALTER TABLE `koin`.`callvan_rooms`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'callvan rooms 고유 id' ,
    CHANGE COLUMN `user_id` `user_id` INT UNSIGNED NOT NULL COMMENT '방장을 맡은 user 고유 id' ,
    CHANGE COLUMN `departure_place` `departure_place` VARCHAR(100) NOT NULL COMMENT '출발 장소' ,
    CHANGE COLUMN `departure_datetime` `departure_datetime` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '출발 시간' ,
    CHANGE COLUMN `arrival_place` `arrival_place` VARCHAR(100) NOT NULL COMMENT '도착 장소' ,
    CHANGE COLUMN `maximum_people` `maximum_people` INT UNSIGNED NOT NULL DEFAULT '2' COMMENT '최대 인원' ,
    CHANGE COLUMN `current_people` `current_people` INT UNSIGNED NOT NULL DEFAULT '1' COMMENT '현재 인원' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;

ALTER TABLE `koin`.`callvan_participants`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'callvan participants 고유 id' ,
    CHANGE COLUMN `room_id` `room_id` INT UNSIGNED NOT NULL COMMENT 'room 고유 id' ,
    CHANGE COLUMN `user_id` `user_id` INT UNSIGNED NOT NULL COMMENT 'user 고유 id' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;

ALTER TABLE `koin`.`callvan_companies`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'callvan companies 고유 id' ,
    CHANGE COLUMN `name` `name` VARCHAR(100) NOT NULL COMMENT '콜벤 회사 이름' ,
    CHANGE COLUMN `phone` `phone` VARCHAR(100) NOT NULL COMMENT '콜벤 회사 전화번호' ,
    CHANGE COLUMN `pay_card` `pay_card` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '카드 여부(0:미사용 / 1:사용)' ,
    CHANGE COLUMN `pay_bank` `pay_bank` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '계좌이체 여부(0: 미사용 / 1:사용)' ,
    CHANGE COLUMN `hit` `hit` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '전화 횟수' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;

ALTER TABLE `koin`.`boards`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'board 고유 id' ,
    CHANGE COLUMN `tag` `tag` VARCHAR(10) NOT NULL COMMENT '게시판 태그' ,
    CHANGE COLUMN `name` `name` VARCHAR(50) NOT NULL COMMENT '게시판 이름' ,
    CHANGE COLUMN `is_anonymous` `is_anonymous` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '익명 닉네임을 사용하는지 여부' ,
    CHANGE COLUMN `article_count` `article_count` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '게시글 개수' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ,
    CHANGE COLUMN `is_notice` `is_notice` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '공지사항인지 여부' ;

ALTER TABLE `koin`.`articles`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'articlees 고유 id' ,
    CHANGE COLUMN `board_id` `board_id` INT UNSIGNED NOT NULL COMMENT '게시판의 고유 id' ,
    CHANGE COLUMN `title` `title` VARCHAR(255) NOT NULL COMMENT '제목' ,
    CHANGE COLUMN `content` `content` MEDIUMTEXT NOT NULL COMMENT '내용' ,
    CHANGE COLUMN `user_id` `user_id` INT UNSIGNED NOT NULL COMMENT '작성자 user 고유 id' ,
    CHANGE COLUMN `nickname` `nickname` VARCHAR(50) NOT NULL COMMENT '작성자 user 닉네임' ,
    CHANGE COLUMN `hit` `hit` INT UNSIGNED NOT NULL DEFAULT '0' COMMENT '조회수' ,
    CHANGE COLUMN `ip` `ip` VARCHAR(45) NOT NULL COMMENT 'IP 주소' ,
    CHANGE COLUMN `is_solved` `is_solved` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '해결 여부(문의 게시판에 사용)' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ,
    CHANGE COLUMN `comment_count` `comment_count` TINYINT UNSIGNED NOT NULL DEFAULT '0' COMMENT '전체 댓글 수' ,
    CHANGE COLUMN `meta` `meta` TEXT NULL DEFAULT NULL COMMENT '등록일자 / 공지사항 주소 등 정보를 String으로 저장' ,
    CHANGE COLUMN `is_notice` `is_notice` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '공지사항인지 여부' ,
    CHANGE COLUMN `notice_article_id` `notice_article_id` INT UNSIGNED NULL DEFAULT NULL COMMENT '공지사항 고유 id' ;

ALTER TABLE `koin`.`article_view_logs`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'article view logs 고유 id' ,
    CHANGE COLUMN `article_id` `article_id` INT UNSIGNED NOT NULL COMMENT 'article 고유 id' ,
    CHANGE COLUMN `user_id` `user_id` INT UNSIGNED NULL DEFAULT NULL COMMENT '본 사람 user 고유 id' ,
    CHANGE COLUMN `expired_at` `expired_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '만료 시간' ,
    CHANGE COLUMN `ip` `ip` VARCHAR(45) NOT NULL COMMENT 'IP 주소' ;

ALTER TABLE `koin`.`admins`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'admins 고유 id' ,
    CHANGE COLUMN `user_id` `user_id` INT UNSIGNED NOT NULL COMMENT '해당 user 고유 id' ,
    CHANGE COLUMN `grant_user` `grant_user` TINYINT(1) NOT NULL DEFAULT '0' COMMENT 'user 수정 권한' ,
    CHANGE COLUMN `grant_callvan` `grant_callvan` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '콜벤 수정 권한' ,
    CHANGE COLUMN `grant_land` `grant_land` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '복덕방 수정 권한' ,
    CHANGE COLUMN `grant_community` `grant_community` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '커뮤니티 수정 권한' ,
    CHANGE COLUMN `grant_shop` `grant_shop` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '상점 수정 권한' ,
    CHANGE COLUMN `grant_version` `grant_version` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '버전 수정 권한' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ,
    CHANGE COLUMN `grant_market` `grant_market` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '거래 수정 권한' ,
    CHANGE COLUMN `grant_circle` `grant_circle` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '동아리 수정 권한' ,
    CHANGE COLUMN `grant_lost` `grant_lost` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '분실물 수정 권한' ,
    CHANGE COLUMN `grant_survey` `grant_survey` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '조사 수정 구너한' ,
    CHANGE COLUMN `grant_bcsdlab` `grant_bcsdlab` TINYINT(1) NOT NULL DEFAULT '0' COMMENT 'bcsdlab 홈페이지 수정 권한' ,
    CHANGE COLUMN `grant_event` `grant_event` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '이벤트 수정 권한' ;

ALTER TABLE `koin`.`activities`
    CHANGE COLUMN `id` `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'activities 고유 id' ,
    CHANGE COLUMN `title` `title` VARCHAR(255) NOT NULL COMMENT '활동명' ,
    CHANGE COLUMN `description` `description` TEXT NULL DEFAULT NULL COMMENT '활동 설명' ,
    CHANGE COLUMN `image_urls` `image_urls` TEXT NULL DEFAULT NULL COMMENT '이미지 링크' ,
    CHANGE COLUMN `date` `date` DATE NOT NULL COMMENT '활동 일자' ,
    CHANGE COLUMN `is_deleted` `is_deleted` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부' ,
    CHANGE COLUMN `created_at` `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자' ,
    CHANGE COLUMN `updated_at` `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자' ;
