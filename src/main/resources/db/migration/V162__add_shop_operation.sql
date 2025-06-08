CREATE TABLE IF NOT EXISTS `koin`.`shop_operation`
(
    `id`            INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `shop_id`       INT UNSIGNED NOT NULL UNIQUE COMMENT '상점 ID',
    `is_open`       tinyint(1)   NOT NULL DEFAULT 0 COMMENT '현재 상점 오픈 여부',
    `is_deleted`    tinyint(1)   NOT NULL DEFAULT 0 COMMENT '삭제 여부',
    `created_at`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    PRIMARY KEY (`id`)
    );

CREATE INDEX idx_shop_operation_shop_id ON shop_operation (shop_id);
CREATE INDEX idx_shop_operation_shop_id_is_open ON shop_operation (shop_id, is_open);
