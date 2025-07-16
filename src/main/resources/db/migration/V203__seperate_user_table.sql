ALTER TABLE coop
    ALTER COLUMN coop_id SET NOT NULL,
  ADD CONSTRAINT uq_coop_id UNIQUE (coop_id);


ALTER TABLE owners
    MODIFY COLUMN account VARCHAR(11) NOT NULL,
    MODIFY COLUMN company_registration_number VARCHAR(12) CHARACTER SET utf8mb3 NOT NULL;


ALTER TABLE users
    MODIFY COLUMN user_id VARCHAR(255),
    ADD CONSTRAINT uq_users_login_id UNIQUE (user_id),
    ADD CONSTRAINT uq_users_nickname UNIQUE (nickname),
    ADD CONSTRAINT uq_users_phone_number UNIQUE (phone_number),
    ADD CONSTRAINT uq_users_email UNIQUE (email);


ALTER TABLE admins
    ADD COLUMN login_id VARCHAR(255),
    ADD COLUMN email VARCHAR(255);

UPDATE admins a
    JOIN users u ON a.user_id = u.id
    SET a.login_id = u.user_id,
        a.email = u.email;

ALTER TABLE admins
    MODIFY COLUMN login_id VARCHAR(255) NOT NULL,
    MODIFY COLUMN email VARCHAR(255) NOT NULL,
    ADD CONSTRAINT uq_admin_login_id UNIQUE (login_id),
    ADD CONSTRAINT uq_admin_email UNIQUE (email);
