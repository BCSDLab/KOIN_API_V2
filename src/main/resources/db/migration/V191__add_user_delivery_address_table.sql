CREATE TABLE IF NOT EXISTS `koin`.`user_delivery_address`
(
    `id`              INT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '배달 주소 고유 ID',
    `user_id`         INT UNSIGNED   NOT NULL COMMENT '사용자 ID',
    `address_type`    VARCHAR(20)    NOT NULL COMMENT '주소 타입 (CAMPUS, OFF_CAMPUS)',

    -- 교내 주소용 필드
    `campus_delivery_address_id`     INT UNSIGNED NULL COMMENT '교내 배달 주소 ID',

    -- 교외 주소용 필드
    `zip_number`      VARCHAR(10)   NULL COMMENT '교외 배달 주소 우편 번호',
    `si_do`           VARCHAR(50)    NULL COMMENT '시/도',
    `si_gun_gu`       VARCHAR(50)    NULL COMMENT '시/군/구',
    `eup_myeon_dong`  VARCHAR(50)    NULL COMMENT '읍/면/동',
    `road`            VARCHAR(50)    NULL COMMENT '도로명',
    `building_name`   VARCHAR(50)    NULL COMMENT '건물명',
    `detail_address`  VARCHAR(100)   NULL COMMENT '상세 주소',
    `full_address`    VARCHAR(255)   NULL COMMENT '전체 주소 (도로명/지번)',

    -- 배달 기사 요청 사항
    `to_rider`        VARCHAR(150)   NULL COMMENT '배달기사 요청사항',

    -- 사용 추적
    `last_used_at`    TIMESTAMP      NULL      COMMENT '마지막 사용 시간',
    `usage_count`     INT UNSIGNED   DEFAULT 0 COMMENT '사용 횟수',
    `is_default`      tinyint(1)     DEFAULT 0,

    `created_at`      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`      TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    PRIMARY KEY (`id`)
);

CREATE INDEX idx_user_address ON `koin`.`user_delivery_address` (user_id);
