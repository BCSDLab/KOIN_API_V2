CREATE TABLE `koin`.`shop_category_map` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT 'shop_category_map 고유 id',
  `shop_id` int unsigned NOT NULL COMMENT 'shops 고유 id',
  `shop_category_id` int unsigned NOT NULL COMMENT 'shop_categories 고유 id',
  `is_deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '삭제 여부',
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 일자',
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '업데이트 일자',
  PRIMARY KEY (`id`)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8
COLLATE = utf8_bin;

INSERT INTO `koin`.`shop_category_map` (`shop_id`, `shop_category_id`)
SELECT `id`, (
    CASE
        WHEN `category` = 'S001' THEN 10
        WHEN `category` = 'S002' THEN 4
        WHEN `category` = 'S003' THEN 5
        WHEN `category` = 'S004' THEN 6
        WHEN `category` = 'S005' THEN 2
        WHEN `category` = 'S006' THEN 3
        WHEN `category` = 'S008' THEN 7
        WHEN `category` = 'S009' THEN 9
        WHEN `category` = 'S010' THEN 8
        ELSE 10
        END
    )
FROM `koin`.`shops`;