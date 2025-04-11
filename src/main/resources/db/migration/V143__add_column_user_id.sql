ALTER TABLE `users`
    ADD COLUMN `user_id` VARCHAR(255) NOT NULL;

UPDATE `users`
SET `user_id` = SUBSTRING_INDEX(`email`, '@', 1)
WHERE `user_type` IN ('STUDENT', 'ADMIN', 'COUNCIL')
  AND `email` IS NOT NULL
  AND `id` > 0;

UPDATE `users`
SET `user_id` = `phone_number`
WHERE `user_type` = 'OWNER'
  AND `phone_number` IS NOT NULL
  AND `id` > 0;

UPDATE `users`
SET `user_id` = `email`
WHERE `user_type` = 'COOP'
  AND `email` IS NOT NULL
  AND `id` > 0;

ALTER TABLE `users`
    ADD CONSTRAINT USER_ID_UNIQUE UNIQUE (`user_id`);
