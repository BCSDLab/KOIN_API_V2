CREATE INDEX shop_opens_filter ON shop_opens (day_of_week, shop_id, open_time, close_time);
CREATE INDEX shop_opens_shop_id ON shop_opens (shop_id);
