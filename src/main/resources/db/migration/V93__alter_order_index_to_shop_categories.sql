ALTER TABLE shop_categories ADD COLUMN order_index INT;

UPDATE shop_categories sc
JOIN (
    SELECT id, ROW_NUMBER() OVER (ORDER BY id) AS new_order_index
    FROM shop_categories
) AS oc
ON sc.id = oc.id
SET sc.order_index = oc.new_order_index;

ALTER TABLE shop_categories MODIFY order_index INT UNIQUE NOT NULL;
