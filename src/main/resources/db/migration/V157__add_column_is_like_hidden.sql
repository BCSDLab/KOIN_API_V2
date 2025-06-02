ALTER TABLE `koin`.`club`
    ADD COLUMN `is_like_hidden` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '좋아요 숨김 여부';
