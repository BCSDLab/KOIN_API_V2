ALTER TABLE coop
    ALTER COLUMN coop_id SET NOT NULL,
  ADD CONSTRAINT uq_coop_id UNIQUE (coop_id);

