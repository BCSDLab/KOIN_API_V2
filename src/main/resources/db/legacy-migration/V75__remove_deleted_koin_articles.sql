SET SQL_SAFE_UPDATES = 0;

DELETE a, k
FROM `koin`.`new_articles` a
         JOIN `koin`.`new_koin_articles` k ON a.id = k.article_id
WHERE k.is_deleted = 1;

SET SQL_SAFE_UPDATES = 1;
