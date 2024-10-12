CREATE FULLTEXT INDEX idx_fulltext_title ON `koin`.`new_articles` (title) WITH PARSER ngram;
