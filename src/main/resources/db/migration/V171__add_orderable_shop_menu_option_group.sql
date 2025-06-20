CREATE TABLE IF NOT EXISTS `koin`.`orderable_shop_menu_option_group`
(
    `id`                INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `orderable_shop_id` INT UNSIGNED NOT NULL                COMMENT '주문 가능한 상점 ID',
    `name`              VARCHAR(255) NOT NULL                COMMENT '옵션 그룹 이름 (ex: 맛, 음료 등)',
    `description`       TEXT         NULL                    COMMENT '옵션 그룹 설명',
    `is_required`       TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '필수 옵션 여부',
    `min_select`        INT UNSIGNED NOT NULL DEFAULT 0      COMMENT '최소 선택 개수',
    `max_select`        INT UNSIGNED NULL DEFAULT NULL       COMMENT '최대 선택 개수',
    `is_deleted`        TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '삭제 여부',
    `created_at`        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    PRIMARY KEY (`id`)
);

CREATE INDEX idx_orderable_shop_id ON `koin`.`orderable_shop_menu_option_group` (orderable_shop_id);
