CREATE TABLE team_recruitment_notification
(
    id                INTEGER      NOT NULL AUTO_INCREMENT,
    recipient_id      INTEGER      NOT NULL,
    notification_type VARCHAR(30)  NOT NULL,
    recruitment_id    INTEGER,
    application_id    INTEGER,
    chat_room_id      INTEGER,
    sender_nickname   VARCHAR(50),
    message_preview   VARCHAR(100),
    is_read           TINYINT(1)   NOT NULL DEFAULT 0,
    is_deleted        TINYINT(1)   NOT NULL DEFAULT 0,
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_notification_recipient FOREIGN KEY (recipient_id) REFERENCES user (id)
);
