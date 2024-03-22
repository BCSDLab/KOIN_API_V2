CREATE TABLE `koin`.`shop_images` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'shop_images 고유 id',
  `shop_id` int unsigned NOT NULL COMMENT 'shops 고유 id',
  `image_url` varchar(255) DEFAULT NULL COMMENT '이미지 URL',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8
COLLATE = utf8_bin;
