CREATE TABLE IF NOT EXISTS `koin`.`orderable_shop_delivery_option`
(
    `id`                       INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'ID',
    `orderable_shop_id`        INT UNSIGNED NOT NULL COMMENT '주문 가능 상점 ID',
    `campus_delivery`          tinyint(1) NOT NULL DEFAULT 1 COMMENT '교내 배달 가능 여부',
    `off_campus_delivery`      tinyint(1) NOT NULL DEFAULT 1 COMMENT '교외 배달 가능 여부',
    `created_at`  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    PRIMARY KEY (`id`)
);
