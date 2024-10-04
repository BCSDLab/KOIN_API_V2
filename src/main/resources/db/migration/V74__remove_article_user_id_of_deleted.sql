SET SQL_SAFE_UPDATES = 0;

UPDATE `koin`.`new_koin_articles`
SET user_id = NULL
WHERE user_id IN (SELECT id FROM `koin`.`users` WHERE is_deleted = 1)
   OR user_id NOT IN (SELECT id FROM `koin`.`users`);

SET SQL_SAFE_UPDATES = 1;
