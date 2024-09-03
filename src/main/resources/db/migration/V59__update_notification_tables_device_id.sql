INSERT INTO `device` (user_id, fcm_token, last_accessed_at, created_at, updated_at)
SELECT DISTINCT n.users_id, u.device_token, NOW(), NOW(), NOW()
FROM `notification` n
         LEFT JOIN `device` d ON n.users_id = d.user_id
         JOIN `users` u ON n.users_id = u.id
WHERE d.id IS NULL;

INSERT INTO `device` (user_id, model, type, fcm_token, last_accessed_at, created_at, updated_at)
SELECT DISTINCT n.user_id, null, null, u.device_token, NOW(), NOW(), NOW()
FROM `notification_subscribe` n
         LEFT JOIN `device` d ON n.user_id = d.user_id
         JOIN `users` u ON n.user_id = u.id
WHERE d.id IS NULL;


UPDATE notification n
    JOIN device d ON n.users_id = d.user_id
    SET n.device_id = d.id;

UPDATE notification_subscribe n
    JOIN device d ON n.user_id = d.user_id
    SET n.device_id = d.id;
