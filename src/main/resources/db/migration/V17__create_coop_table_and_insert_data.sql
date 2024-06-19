CREATE TABLE IF NOT EXISTS `coop`
(
    `user_id` INT UNSIGNED NOT NULL COMMENT '유저 id, user_type COOP으로 가져옴',
    `coop_id` VARCHAR(255) COMMENT '영양사 id, 일반 로그인 형식',
    PRIMARY KEY (`user_id`),
    CONSTRAINT `FK_COOP_ON_USER` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
);

INSERT INTO `coop` (`user_id`, `coop_id`)
    SELECT `id`, '' FROM `users` WHERE `user_type` = 'COOP';
