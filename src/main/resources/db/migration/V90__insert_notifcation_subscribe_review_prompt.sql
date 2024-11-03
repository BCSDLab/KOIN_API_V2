INSERT INTO notification_subscribe (created_at, updated_at, subscribe_type, user_id)
SELECT NOW(), NOW(), 'REVIEW_PROMPT', s.user_id
FROM students s
         JOIN users u ON s.user_id = u.id;
