CREATE TABLE `owners` (
  `user_id` int NOT NULL,
  `company_registration_number` varchar(12) NOT NULL,
  `company_registration_number_image_url` varchar(255) NOT NULL,
  `is_authed` tinyint DEFAULT '0',
  `grant_shop` tinyint DEFAULT '0',
  `grant_event` tinyint DEFAULT '0',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `company_registration_number_UNIQUE` (`company_registration_number`)
)
ENGINE = InnoDB
DEFAULT CHARSET = utf8
COLLATE = utf8_bin;