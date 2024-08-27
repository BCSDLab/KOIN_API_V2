ALTER TABLE `boards`
    DROP COLUMN tag,
    DROP COLUMN seq;

ALTER TABLE `notice_articles`
    RENAME TO `koreatech_articles`,
    DROP COLUMN has_notice,
    DROP COLUMN is_deleted,
    ADD COLUMN attachment JSON COMMENT '첨부 파일',
    CHANGE COLUMN permalink url VARCHAR(255) NOT NULL COMMENT '기존 게시글 url';
