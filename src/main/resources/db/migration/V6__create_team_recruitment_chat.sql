CREATE TABLE team_recruitment_chat_room
(
    id               INTEGER      NOT NULL AUTO_INCREMENT,
    recruitment_id   INTEGER      NOT NULL,
    room_name        VARCHAR(100) NOT NULL,
    room_type        VARCHAR(10)  NOT NULL,
    status           VARCHAR(15)  NOT NULL DEFAULT 'ACTIVE',
    max_member_count INTEGER      NOT NULL,
    created_at       TIMESTAMP    NOT NULL,
    updated_at       TIMESTAMP    NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE team_recruitment_chat_room_member
(
    id                   INTEGER   NOT NULL AUTO_INCREMENT,
    chat_room_id         INTEGER   NOT NULL,
    user_id              INTEGER   NOT NULL,
    last_read_message_id INTEGER,
    joined_at            TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_chat_room_member (chat_room_id, user_id),
    CONSTRAINT fk_chat_member_room FOREIGN KEY (chat_room_id) REFERENCES team_recruitment_chat_room (id),
    CONSTRAINT fk_chat_member_user FOREIGN KEY (user_id) REFERENCES user (id)
);

CREATE TABLE team_recruitment_chat_message
(
    id           INTEGER   NOT NULL AUTO_INCREMENT,
    chat_room_id INTEGER   NOT NULL,
    sender_id    INTEGER   NOT NULL,
    content      TEXT      NOT NULL,
    is_image     TINYINT(1) NOT NULL DEFAULT 0,
    created_at   TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_chat_message_room   FOREIGN KEY (chat_room_id) REFERENCES team_recruitment_chat_room (id),
    CONSTRAINT fk_chat_message_sender FOREIGN KEY (sender_id) REFERENCES user (id)
);
