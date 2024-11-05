CREATE TABLE `shop_main_categories`
(
    id                      INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT 'shop_main_categories 고유 id',
    name                    VARCHAR(255) NOT NULL COMMENT '메인 카테고리 이름',
    notification_message_id INT UNSIGNED NOT NULL COMMENT '알림 메시지 id',
    created_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성 일자',
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자',
    CONSTRAINT `FK_MAIN_CATEGORIES_ON_SHOP_NOTIFICATION_MESSAGES`
        FOREIGN KEY (`notification_message_id`) REFERENCES `shop_notification_messages` (`id`)
);

INSERT INTO `shop_main_categories` (name, notification_message_id)
VALUES ('가게', 1),
       ('콜밴', 2),
       ('뷰티', 3);
