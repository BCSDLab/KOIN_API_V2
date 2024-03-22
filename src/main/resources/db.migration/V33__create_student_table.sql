CREATE TABLE `students` (
  `user_id` int NOT NULL,
  `anonymous_nickname` varchar(255) DEFAULT NULL,
  `student_number` varchar(20) DEFAULT NULL,
  `major` varchar(50) DEFAULT NULL,
  `is_graduated` tinyint(1) NOT NULL DEFAULT '0',
  `auth_token` varchar(255) DEFAULT NULL,
  `auth_expired_at` varchar(255) DEFAULT NULL,
  `reset_token` varchar(255) DEFAULT NULL,
  `reset_expired_at` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `anonymous_nickname_UNIQUE` (`anonymous_nickname`))
ENGINE = InnoDB
DEFAULT CHARSET = utf8
COLLATE = utf8_bin;