CREATE TABLE IF NOT EXISTS shop_benefit_categories (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(60) NOT NULL,
    detail VARCHAR(255),
    created_at  timestamp  default CURRENT_TIMESTAMP not null,
    updated_at  timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS shop_benefit_category_map (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    shop_id INT UNSIGNED NOT NULL,
    benefit_id INT UNSIGNED NOT NULL,
    created_at  timestamp  default CURRENT_TIMESTAMP not null,
    updated_at  timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP,
    FOREIGN KEY (`shop_id`) references `shops` (`id`),
    FOREIGN KEY (`benefit_id`) references `shop_benefit_categories` (`id`)
);
