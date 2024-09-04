CREATE TABLE course_type
(
    id         INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    is_deleted tinyint               DEFAULT 0,
    created_at timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE standard_graduation_requirements
(
    id              INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    year            VARCHAR(255) NOT NULL,
    department      VARCHAR(255) NOT NULL,
    course_type_id  INT UNSIGNED,
    required_grades INT          NOT NULL,
    FOREIGN KEY (course_type_id) REFERENCES course_type (id),
    is_deleted      tinyint               DEFAULT 0,
    created_at      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE student_course_calculation
(
    id                                  INT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    user_id                             INT UNSIGNED NOT NULL,
    standard_graduation_requirements_id INT UNSIGNED,
    completed_grades                    INT       NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (standard_graduation_requirements_id) REFERENCES standard_graduation_requirements (id),
    is_deleted                          tinyint            DEFAULT 0,
    created_at                          timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                          timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
