CREATE TABLE IF NOT EXISTS `koin`.`campus_delivery_address_type`
(
    `id` INT UNSIGNED   NOT NULL AUTO_INCREMENT COMMENT '교내 주소 타입 ID',
    `name` VARCHAR(255) NOT NULL COMMENT '교내 주소 타입 이름',
    `created_at`  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
    `updated_at`  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    PRIMARY KEY (`id`)
);

CREATE INDEX idx_campus_delivery_address_type ON `koin`.`campus_delivery_address_type` (id, name);

INSERT INTO `koin`.`campus_delivery_address_type` (`name`) VALUES ('기숙사'), ('공학관'), ('그 외');
