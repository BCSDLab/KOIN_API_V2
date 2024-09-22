ALTER TABLE `versions`
    ADD COLUMN (
    `title`         VARCHAR(255)                            COMMENT '업데이트 문구 제목',
    `is_previous`   tinyint      NOT NULL    DEFAULT 0   COMMENT '지난 버전 여부'
    ),
    DROP CONSTRAINT `versions_type_unique`
;

CREATE TABLE `version_contents` (
    `id`            INT UNSIGNED    NOT NULL    AUTO_INCREMENT      PRIMARY KEY,
    `version_id`    INT UNSIGNED    NOT NULL    COMMENT '업데이트 문구 소제목',
    `title`         VARCHAR(255)                COMMENT '업데이트 문구 본문',
    `content`       TEXT,
    `created_at`    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    TIMESTAMP       DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`version_id`) REFERENCES versions (`id`)
);
