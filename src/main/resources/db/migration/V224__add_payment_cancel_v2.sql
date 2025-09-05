CREATE TABLE IF NOT EXISTS `koin`.`payment_cancel`
(
    `id`                                INT UNSIGNED    NOT NULL    AUTO_INCREMENT COMMENT '결제 취소 ID',
    `payment_id`                        INT UNSIGNED    NOT NULL    COMMENT '결제 ID',
    `transaction_key`                   VARCHAR(64)     NOT NULL    COMMENT '취소 트랜잭션 키',
    `reason`                            VARCHAR(200)    NOT NULL    COMMENT '취소 사유',
    `amount`                            INT UNSIGNED    NOT NULL    COMMENT '취소 금액',
    `canceled_at`                       TIMESTAMP       NOT NULL    COMMENT '취소 일시',
    `is_deleted`                        TINYINT(1)      NOT NULL    DEFAULT '0' COMMENT '삭제 여부',
    `created_at`                        TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일시',
    `updated_at`                        TIMESTAMP       NOT NULL    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일시',
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_payment_cancel_payment` FOREIGN KEY (`payment_id`) REFERENCES `koin`.`payment` (`id`)
);
