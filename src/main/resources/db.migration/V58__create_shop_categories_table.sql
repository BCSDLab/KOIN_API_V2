CREATE TABLE `koin`.`shop_categories` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'shop_categories 고유 id',
  `name` varchar(255) NOT NULL COMMENT '카테고리 이름',
  `image_url` varchar(255) DEFAULT NULL COMMENT '이미지 URL',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8
COLLATE = utf8_bin;

INSERT INTO `koin`.`shop_categories` (`name`, `image_url`) VALUES ('전체보기', 'https://static.koreatech.in/assets/img/img-rest-etc.png');
INSERT INTO `koin`.`shop_categories` (`name`, `image_url`) VALUES ('치킨', 'https://static.koreatech.in/assets/img/img-rest-chicken.png');
INSERT INTO `koin`.`shop_categories` (`name`, `image_url`) VALUES ('피자', 'https://static.koreatech.in/assets/img/img-rest-pizza.png');
INSERT INTO `koin`.`shop_categories` (`name`, `image_url`) VALUES ('도시락', 'https://static.koreatech.in/assets/img/img-rest-box.png');
INSERT INTO `koin`.`shop_categories` (`name`, `image_url`) VALUES ('족발', 'https://static.koreatech.in/assets/img/img-rest-porkfeet.png');
INSERT INTO `koin`.`shop_categories` (`name`, `image_url`) VALUES ('중국집', 'https://static.koreatech.in/assets/img/img-rest-blacknoodle.png');
INSERT INTO `koin`.`shop_categories` (`name`, `image_url`) VALUES ('일반음식점', 'https://static.koreatech.in/assets/img/img-rest-normal.png');
INSERT INTO `koin`.`shop_categories` (`name`, `image_url`) VALUES ('카페', 'https://static.koreatech.in/assets/img/img-rest-cafe.png');
INSERT INTO `koin`.`shop_categories` (`name`, `image_url`) VALUES ('미용실', 'https://static.koreatech.in/assets/img/img-rest-salon.png');
INSERT INTO `koin`.`shop_categories` (`name`, `image_url`) VALUES ('기타', 'https://static.koreatech.in/assets/img/img-rest-etc.png');

