CREATE TABLE update_versions (
    id          INT UNSIGNED    NOT NULL    AUTO_INCREMENT      PRIMARY KEY,
    type        VARCHAR(50)    NOT NULL    UNIQUE,
    version     VARCHAR(50)    NOT NULL,
    title       VARCHAR(255)    NOT NULL,
    created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE update_contents (
    id          INT UNSIGNED    NOT NULL    AUTO_INCREMENT      PRIMARY KEY,
    type_id     INT UNSIGNED    NOT NULL,
    title       VARCHAR(255)    NOT NULL,
    content     TEXT            NOT NULL,
    created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (type_id) REFERENCES update_versions (id)
);

CREATE TABLE update_history (
    id          INT UNSIGNED    NOT NULL    AUTO_INCREMENT      PRIMARY KEY,
    type        VARCHAR(50)     NOT NULL,
    version     VARCHAR(50)     NOT NULL,
    title       VARCHAR(255)    NOT NULL,
    created_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
);
