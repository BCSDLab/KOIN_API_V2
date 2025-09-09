CREATE TABLE IF NOT EXISTS `koin`.`order_takeout_v2`
(
    `order_id`                  INT UNSIGNED    NOT NULL                COMMENT '주문 ID',
    `to_owner`                  VARCHAR(50)     NOT NULL                COMMENT '사장님 전달 메시지',
    `provide_cutlery`           TINYINT(1)      NOT NULL    DEFAULT 0   COMMENT '수저, 포크 수령 여부',
    `packaged_at`               TIMESTAMP       NULL                    COMMENT '표장 완료 일시',
    `estimated_packaged_at`     TIMESTAMP       NULL                    COMMENT '표장 완료 예상 일시',
    `created_at`                TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at`                TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (order_id),
    CONSTRAINT `fk_order_takeout_order_v2` FOREIGN KEY (`order_id`) REFERENCES `koin`.`order_v2` (`id`)
);
