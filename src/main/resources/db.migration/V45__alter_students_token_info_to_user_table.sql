ALTER TABLE `koin`.`users`
    ADD COLUMN `auth_token` VARCHAR(255) NULL DEFAULT NULL COMMENT '이메일 인증 토큰' AFTER `profile_image_url`,
    ADD COLUMN `auth_expired_at` VARCHAR(255) NULL DEFAULT NULL COMMENT '이메일 인증 토큰 만료 시간' AFTER `auth_token`,
    ADD COLUMN `reset_token` VARCHAR(255) NULL DEFAULT NULL COMMENT '비밀번호 초기화 토큰' AFTER `auth_expired_at`,
    ADD COLUMN `reset_expired_at` VARCHAR(255) NULL DEFAULT NULL COMMENT '비밀번호 초기화 토큰 만료 시간' AFTER `reset_token`,
    CHANGE COLUMN `profile_image_url` `profile_image_url` VARCHAR(255) NULL DEFAULT NULL COMMENT '프로필 이미지 s3 url' AFTER `last_logged_at`;

ALTER TABLE `koin`.`students`
DROP COLUMN `reset_expired_at`,
DROP COLUMN `reset_token`,
DROP COLUMN `auth_expired_at`,
DROP COLUMN `auth_token`;
