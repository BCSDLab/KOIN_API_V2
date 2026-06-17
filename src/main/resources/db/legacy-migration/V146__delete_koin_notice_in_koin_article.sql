DELETE nka
FROM new_koin_articles nka
INNER JOIN admins a ON nka.user_id = a.user_id
WHERE nka.id = nka.id;
