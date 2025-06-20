CREATE TABLE IF NOT EXISTS `koin`.`orderable_shop_menu`
(
    `id`                INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `orderable_shop_id` INT UNSIGNED NOT NULL                COMMENT '주문 가능한 상점 ID',
    `name`              VARCHAR(255) NOT NULL                COMMENT '메뉴 이름',
    `description`       TEXT         NULL                    COMMENT '메뉴 설명',
    `is_sold_out`       TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '품절 여부',
    `is_deleted`        TINYINT(1)   NOT NULL DEFAULT 0      COMMENT '삭제 여부',
    `created_at`        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    PRIMARY KEY (`id`)
);

CREATE INDEX idx_orderable_shop_menu_orderable_shop_id ON `koin`.`orderable_shop_menu` (orderable_shop_id);
