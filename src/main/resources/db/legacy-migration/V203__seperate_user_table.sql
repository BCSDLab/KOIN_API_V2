-- 영양사
ALTER TABLE coop
    MODIFY COLUMN coop_id VARCHAR(255) NOT NULL;

-- 사장님
ALTER TABLE owners
    MODIFY COLUMN account VARCHAR (11) NOT NULL,
    MODIFY COLUMN company_registration_number VARCHAR (12) CHARACTER SET utf8mb3 NOT NULL;


-- 어드민
ALTER TABLE admins
    ADD COLUMN login_id VARCHAR(255),
    ADD COLUMN email VARCHAR(255);

UPDATE admins a
    JOIN users u
ON a.user_id = u.id
    SET a.login_id = u.user_id, a.email = u.email;

ALTER TABLE admins
    MODIFY COLUMN login_id VARCHAR (255) NOT NULL,
    MODIFY COLUMN email VARCHAR (255) NOT NULL,
    ADD CONSTRAINT uq_admin_login_id UNIQUE (login_id),
    ADD CONSTRAINT uq_admin_email UNIQUE (email);

-- 유저
ALTER TABLE users
    MODIFY COLUMN user_id VARCHAR (255),
    ADD CONSTRAINT uq_users_login_id UNIQUE (user_id),
    ADD CONSTRAINT uq_users_nickname UNIQUE (nickname),
    ADD CONSTRAINT uq_users_phone_number UNIQUE (phone_number),
    ADD CONSTRAINT uq_users_email UNIQUE (email);


ALTER TABLE `users`
    ADD COLUMN `anonymous_nickname` VARCHAR(255) NULL UNIQUE AFTER `is_deleted`;

UPDATE `users` u
    JOIN `students` s
ON u.id = s.user_id
    SET u.anonymous_nickname = s.anonymous_nickname;

UPDATE `users`
SET `anonymous_nickname` = CONCAT('익명_', SUBSTRING(CONCAT(MD5(RAND()), MD5(RAND())), 1, 13))
WHERE `anonymous_nickname` IS NULL;

ALTER TABLE `users`
    MODIFY COLUMN `anonymous_nickname` VARCHAR (255) NOT NULL UNIQUE;


UPDATE users
SET
    user_id = NULL,
    nickname = NULL,
    phone_number = NULL,
    email = NULL
WHERE user_type NOT IN ('GENERAL', 'STUDENT', 'COUNCIL', 'COOP');

