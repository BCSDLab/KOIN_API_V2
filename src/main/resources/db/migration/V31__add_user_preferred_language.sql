ALTER TABLE `users`
    ADD COLUMN `preferred_language` VARCHAR(35) NULL COMMENT '사용자 선택 언어 코드. 미설정 시 한국어 적용';
