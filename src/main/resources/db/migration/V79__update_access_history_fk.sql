ALTER TABLE access_history
DROP FOREIGN KEY FK_ACCESS_HISTORY_ON_DEVICE_ID;

ALTER TABLE access_history
    ADD CONSTRAINT FK_ACCESS_HISTORY_ON_DEVICE_ID
        FOREIGN KEY (device_id)
            REFERENCES device(id) ON DELETE SET NULL;