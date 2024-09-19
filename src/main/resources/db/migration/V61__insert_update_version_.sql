-- 기존 version table에서 'android' 제거 --
DELETE FROM `versions` WHERE `id` = 1;

INSERT INTO `update_versions` (`type`, `version`) VALUES ('ANDROID', '3.1.0');
INSERT INTO `update_versions` (`type`, `version`) VALUES ('IOS', '3.1.0');
INSERT INTO `update_contents` (`type_id`) VALUES (1);
INSERT INTO `update_contents` (`type_id`) VALUES (2);
INSERT INTO `update_history` (`type`, `version`, `created_at`) VALUES ('ANDROID', '3.1.0', '2018-04-17 15:00:00');
INSERT INTO `update_history` (`type`, `version`, `created_at`) VALUES ('IOS', '3.1.0', '2018-04-17 15:00:00');
