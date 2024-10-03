CREATE FULLTEXT INDEX idx_fulltext_title ON `koin`.`articles` (title) WITH PARSER ngram;
