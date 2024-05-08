ALTER TABLE notification_subscribe
    ADD CONSTRAINT unique_user_id_subscribe_type_detail_type
        UNIQUE (user_id, subscribe_type, detail_type);
