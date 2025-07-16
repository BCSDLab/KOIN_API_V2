ALTER TABLE coop
    ALTER COLUMN coop_id SET NOT NULL,
  ADD CONSTRAINT uq_coop_id UNIQUE (coop_id);

ALTER TABLE owners
    MODIFY COLUMN account VARCHAR(11) NOT NULL;

ALTER TABLE owners
    MODIFY COLUMN company_registration_number VARCHAR(12) CHARACTER SET utf8mb3 NOT NULL;
