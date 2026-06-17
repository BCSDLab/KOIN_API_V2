DROP INDEX idx_articles_is_deleted ON new_articles;
CREATE INDEX idx_articles_deleted_notice ON new_articles (is_deleted, is_notice);
