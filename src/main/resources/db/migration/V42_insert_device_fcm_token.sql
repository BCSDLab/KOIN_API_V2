INSERT INTO device (user_id, fcm_token)
SELECT id, device_token
FROM users
WHERE device_token IS NOT NULL;
