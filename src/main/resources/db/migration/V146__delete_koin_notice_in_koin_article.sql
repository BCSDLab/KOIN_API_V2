SET SQL_SAFE_UPDATES = 0;

DELETE nka
FROM new_koin_articles nka
INNER JOIN admins a ON nka.user_id = a.user_id;

SET SQL_SAFE_UPDATES = 1;
