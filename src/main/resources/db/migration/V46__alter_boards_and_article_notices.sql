ALTER TABLE `boards`
    DROP COLUMN tag,
    DROP COLUMN seq;

ALTER TABLE `notice_articles`
    RENAME TO `koreatech_articles`,
    DROP COLUMN has_notice,
    CHANGE COLUMN permalink url VARCHAR(255) NOT NULL COMMENT '기존 게시글 url',
    DROP INDEX ux_notice_article,
    ADD CONSTRAINT ux_koreatech_article UNIQUE (board_id, article_num);
