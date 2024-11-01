INSERT INTO `koin`.`admins` (user_id, team_name, track_name, can_create_admin, super_admin)
SELECT u.id, '코인 어드민', '코인 어드민', 1, 1
FROM `koin`.`users` u
WHERE u.user_type = 'ADMIN'
ORDER BY u.id ASC LIMIT 1;
