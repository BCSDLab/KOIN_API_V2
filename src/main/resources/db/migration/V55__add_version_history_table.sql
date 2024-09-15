CREATE TABLE version_history (
    id          UNSIGNED INT    NOT NULL    AUTO_INCREMENT      PRIMARY KEY,
    type        VARCHAR(255)    NOT NULL,
    version     VARCHAR(255)    NOT NULL,
    date        TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    message     VARCHAR(255)
)
