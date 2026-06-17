INSERT INTO `koin`.`admins` (user_id, team_type, track_type, can_create_admin, super_admin)
SELECT u.id, 'KOIN', 'PL', 1, 1
FROM `koin`.`users` u
WHERE u.user_type = 'ADMIN'
ORDER BY u.id ASC LIMIT 1;
