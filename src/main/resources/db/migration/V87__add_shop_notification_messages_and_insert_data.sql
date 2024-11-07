CREATE TABLE `shop_notification_messages`
(
    id         INT UNSIGNED AUTO_INCREMENT PRIMARY KEY COMMENT 'shop_notification_messages 고유 id',
    title      VARCHAR(255)                        NOT NULL COMMENT '메세지 제목',
    content    VARCHAR(255)                        NOT NULL COMMENT '메세지 내용',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '생성 일자',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 일자'
);

INSERT INTO `shop_notification_messages` (title, content)
VALUES (', 맛있게 드셨나요?', '드신 메뉴에 대한 리뷰를 작성해보세요!'),
       (', 편안하게 이동하셨나요?', '승차하신 콜벤에 대한 리뷰를 작성해보세요!'),
       (', 어떠셨나요?', '이용하신 샵에 대한 리뷰를 작성해보세요!');
