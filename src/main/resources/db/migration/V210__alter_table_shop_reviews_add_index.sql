CREATE INDEX idx_shop_reviews_covering ON shop_reviews(shop_id, is_deleted, rating);
