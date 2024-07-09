CREATE TABLE IF NOT EXISTS `shop_reviews` (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    shop_id INT UNSIGNED NOT NULL,
    content TEXT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 0 AND 5),
    reviewer_id INT UNSIGNED NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`reviewer_id`) references `users` (`id`),
    FOREIGN KEY (`shop_id`) references `shops` (`id`)
);


CREATE TABLE IF NOT EXISTS `shop_review_images` (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    review_id INT UNSIGNED NOT NULL,
    image_urls TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`review_id`) REFERENCES `shop_reviews`(`id`)
);

CREATE TABLE IF NOT EXISTS `shop_review_menus` (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    review_id INT UNSIGNED NOT NULL,
    menu_name TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`review_id`) REFERENCES `shop_reviews`(`id`)
);

CREATE TABLE IF NOT EXISTS `shop_review_reports` (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    review_id INT UNSIGNED NOT NULL,
    reason_title VARCHAR(50) NOT NULL,
    reason_detail VARCHAR(255) NOT NULL,
    reported_by INT UNSIGNED NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (`review_id`) REFERENCES `shop_reviews`(`id`),
    FOREIGN KEY (`reported_by`) references `users`(`id`)
);

CREATE TABLE IF NOT EXISTS `shop_review_reports_categories` (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    detail VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO `shop_review_reports_categories` (name, detail) VALUES
('주제에 맞지 않음', '해당 음식점과 관련 없는 리뷰입니다.'),
('스팸', '광고가 포함된 리뷰입니다.'),
('욕설', '욕설, 성적인 언어, 비방하는 글이 포함된 리뷰입니다.'),
('기타', '');
