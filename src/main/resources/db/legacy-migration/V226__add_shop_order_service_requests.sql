CREATE TABLE IF NOT EXISTS `koin`.`shop_order_service_requests`
(
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
  `shop_id` INT UNSIGNED NOT NULL COMMENT '식당 ID',
  `minimum_order_amount` INT UNSIGNED NOT NULL COMMENT '최소 주문 금액',
  `is_takeout` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '포장 여부',
  `delivery_option` VARCHAR(50) NOT NULL COMMENT '배달 옵션',
  `campus_delivery_tip` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '캠퍼스 내 배달 팁',
  `off_campus_delivery_tip` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '캠퍼스 외 배달 팁',
  `business_license_url` VARCHAR(255) NOT NULL COMMENT '사업자 등록증 URL',
  `business_certificate_url` VARCHAR(255) NOT NULL COMMENT '영업 신고증 URL',
  `bank_copy_url` VARCHAR(255) NOT NULL COMMENT '통장 사본 URL',
  `bank` VARCHAR(10) NOT NULL COMMENT '은행명',
  `account_number` VARCHAR(20) NOT NULL COMMENT '계좌 번호',
  `request_status` VARCHAR(50) NOT NULL DEFAULT 'PENDING' COMMENT '요청 상태',
  `approved_at` TIMESTAMP NULL DEFAULT NULL COMMENT '승인 일자',
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`),
  CONSTRAINT fk_shop_order_service_requests_shop_id FOREIGN KEY (`shop_id`) REFERENCES `koin`.`shops`(`id`) ON DELETE CASCADE
);
