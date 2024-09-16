CREATE TABLE version_message (
    id          UNSIGNED INT    NOT NULL    AUTO_INCREMENT      PRIMARY KEY,
    type_id     UNSIGNED INT    NOT NULL,
    title       VARCHAR(255)    NOT NULL,
    content     TEXT            NOT NULL,
    created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (type_id) REFERENCES versions (id),
);

CREATE TABLE version_history (
    id          UNSIGNED INT    NOT NULL    AUTO_INCREMENT      PRIMARY KEY,
    type_id     UNSIGNED INT    NOT NULL,
    version     VARCHAR(255)    NOT NULL,
    date        TIMESTAMP       DEFAULT CURRENT_TIMESTAMP
    FOREIGN KEY (type_id) REFERENCES versions (id),
);

ALTER TABLE versions
    ADD COLUMN title VARCHAR(255);
