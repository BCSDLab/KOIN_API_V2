CREATE TABLE IF NOT EXISTS `koin`.`cart`
(
    `id`                              INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '고유 ID',
    `user_id`                         INT UNSIGNED NOT NULL                COMMENT '사용자 ID',
    `orderable_shop_id`               INT UNSIGNED NOT NULL                COMMENT '주문 가능 상점 ID',
    `created_at`                      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`                      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_orderable_shop` (`user_id`, `orderable_shop_id`)
);

CREATE INDEX idx_user_id_orderable_shop_id ON `koin`.`cart` (user_id, orderable_shop_id);
CREATE INDEX idx_orderable_shop_id ON `koin`.`cart` (orderable_shop_id);
