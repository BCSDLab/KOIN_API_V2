CREATE TABLE `coop_names`
(
    `id`        INT UNSIGNED AUTO_INCREMENT NOT NULL comment '고유 id' primary key,
    `name`      VARCHAR(255) NOT NULL comment '생협 운영장 이름'
);

INSERT INTO `coop_names` (`name`)
VALUES ('학생식당'),
       ('복지관식당'),
       ('대즐'),
       ('서점'),
       ('세탁소'),
       ('복사실'),
       ('복지관 참빛관 편의점'),
       ('미용실'),
       ('오락실'),
       ('우편취급국'),
       ('안경원');