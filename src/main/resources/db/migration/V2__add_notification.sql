ALTER TABLE `users` ADD COLUMN device_token VARCHAR(255);

CREATE TABLE `notification`(
    id BIGINT AUTO_INCREMENT NOT NULL comment '고유 id',
    url VARCHAR(255) NOT NULL comment '앱 url',
    title VARCHAR(255) NOT NULL comment '제목',
    message VARCHAR(255) NOT NULL comment '메시지 내용',
    image_url VARCHAR(255) NOT NULL comment '이미지 url',
    `type` VARCHAR(255) NOT NULL comment '알림 타입',
    users_id INT UNSIGNED NOT NULL comment '유저 id',
    is_read TINYINT(1) NOT NULL comment '읽음 여부',
    created_at DATETIME NULL comment '생성 일자',
    updated_at DATETIME NULL comment '수정 일자',
    PRIMARY KEY(`id`),
    CONSTRAINT `FK_NOTIFICATION_ON_USER FOREIGN KEY` FOREIGN KEY (`users_id`) REFERENCES `users` (`id`)
);
