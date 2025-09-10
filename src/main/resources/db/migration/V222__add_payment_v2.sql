CREATE TABLE IF NOT EXISTS `koin`.`payment_v2`
(
    `id`                                INT UNSIGNED    NOT NULL    AUTO_INCREMENT COMMENT '고유 ID',
    `order_id`                          INT UNSIGNED    NOT NULL    COMMENT '주문 번호',
    `payment_key`                       VARCHAR(200)    NOT NULL    COMMENT '결제 키',
    `amount`                            INT UNSIGNED    NOT NULL    COMMENT '결제 금액',
    `status`                            VARCHAR(30)     NOT NULL    COMMENT '결제 상태',
    `method`                            VARCHAR(30)     NOT NULL    COMMENT '결제 수단',
    `description`                       VARCHAR(255)    NOT NULL    COMMENT '결제 설명',
    `easy_pay_company`                  VARCHAR(255)    NULL        COMMENT '간편 결제사',
    `requested_at`                      TIMESTAMP       NOT NULL    COMMENT '결제 요청 일시',
    `approved_at`                       TIMESTAMP       NOT NULL    COMMENT '결제 승인 일시',
    `receipt`                           TEXT            NOT NULL    COMMENT '영수증',
    `is_deleted`                        TINYINT(1)      NOT NULL    DEFAULT '0' COMMENT '삭제 여부',
    `created_at`                        TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at`                        TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    UNIQUE KEY uq_payment_key (`id`),
    CONSTRAINT fk_payment_order_v2 FOREIGN KEY (`order_id`) REFERENCES `koin`.`order_v2` (`id`)
);
