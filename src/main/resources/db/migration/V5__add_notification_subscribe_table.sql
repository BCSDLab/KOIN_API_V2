CREATE TABLE notification_subscribe
(
    id             BIGINT AUTO_INCREMENT NOT NULL,
    created_at     datetime              NOT NULL,
    updated_at     datetime              NOT NULL,
    subscribe_type VARCHAR(255)          NOT NULL,
    user_id        BIGINT                NOT NULL,
    CONSTRAINT pk_notification_subscribe PRIMARY KEY (id)
);

ALTER TABLE notification_subscribe
    ADD CONSTRAINT FK_NOTIFICATION_SUBSCRIBE_ON_USER FOREIGN KEY (user_id) REFERENCES users (id);
