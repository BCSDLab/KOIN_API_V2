CREATE TABLE IF NOT EXISTS `koin`.`shop_menu_origin`
(
    `id`            INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `shop_id`       INT UNSIGNED NOT NULL           COMMENT '상점 ID',
    `ingredient`    TEXT         NULL               COMMENT '재료 정보',
    `origin`        TEXT         NULL               COMMENT '원산지 정보',
    `is_deleted`    tinyint(1)   NOT NULL DEFAULT 0 COMMENT '삭제 여부',
    `created_at`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    PRIMARY KEY (`id`)
);

CREATE INDEX idx_shop_menu_origin_shop_id ON shop_menu_origin (shop_id);
