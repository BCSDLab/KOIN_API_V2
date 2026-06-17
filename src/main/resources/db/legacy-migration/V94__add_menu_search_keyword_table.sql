CREATE TABLE if not exists `shop_menu_search_keywords`
(
    id              INT UNSIGNED AUTO_INCREMENT NOT NULL,
    keyword         VARCHAR(255)                NOT NULL,
    created_at      timestamp                   default CURRENT_TIMESTAMP NOT NULL,
    updated_at      timestamp                   default CURRENT_TIMESTAMP NOT NULL on update CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
    );
