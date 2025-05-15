ALTER TABLE `koin`.`club_sns`
    ADD CONSTRAINT unique_club_id_sns_type UNIQUE (club_id, sns_type);
