CREATE TABLE shop_benefit_categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(60) NOT NULL,
    detail VARCHAR(255),
    created_at  timestamp  default CURRENT_TIMESTAMP not null
    updated_at  timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
);

-- shop_benefit_categoriy_map 테이블 생성
CREATE TABLE shop_benefit_categoriy_map (
    id INT AUTO_INCREMENT PRIMARY KEY,
    shop_id INT NOT NULL,
    benefit_id INT NOT NULL,
    created_at  timestamp  default CURRENT_TIMESTAMP not null
    updated_at  timestamp  default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP
    CONSTRAINT fk_shop FOREIGN KEY (shop_id) REFERENCES shop(id),
    CONSTRAINT fk_benefit_category FOREIGN KEY (benefit_id) REFERENCES shop_benefit_categories(id)
);
