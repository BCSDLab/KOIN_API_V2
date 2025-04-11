INSERT INTO koin_notice (
    article_id,
    admin_id,
    is_deleted,
    created_at,
    updated_at
)
SELECT
    nka.article_id,
    nka.user_id,
    nka.is_deleted,
    nka.created_at,
    nka.updated_at
FROM
    new_koin_articles nka
INNER JOIN
    admins a
    ON nka.user_id = a.user_id;
