CREATE TABLE IF NOT EXISTS `koin`.`order_delivery_v2`
(
    `order_id`                  INT UNSIGNED    NOT NULL                COMMENT '주문 ID',
    `address`                   TEXT            NOT NULL                COMMENT '배달 주소',
    `address_detail`            TEXT            NULL                    COMMENT '배달 상세 주소',
    `to_owner`                  VARCHAR(50)     NOT NULL                COMMENT '사장님 전달 메시지',
    `to_rider`                  VARCHAR(50)     NOT NULL                COMMENT '라이더 전달 메시지',
    `delivery_tip`              INT UNSIGNED    NOT NULL                COMMENT '배달비',
    `provide_cutlery`           TINYINT(1)      NOT NULL    DEFAULT 0   COMMENT '수저, 포크 수령 여부',
    `dispatched_at`             TIMESTAMP       NULL                    COMMENT '배달 출발 일시',
    `completed_at`              TIMESTAMP       NULL                    COMMENT '배달 완료 일시',
    `estimated_arrival_at`      TIMESTAMP       NULL                    COMMENT '배달 완료 예상 일시',
    `created_at`                TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at`                TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (order_id),
    CONSTRAINT `fk_order_delivery_order_v2` FOREIGN KEY (`order_id`) REFERENCES `koin`.`order_v2` (`id`)
);
