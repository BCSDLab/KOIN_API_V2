DROP INDEX idx_shop_operation_shop_id_is_open ON shop_operation;
DROP INDEX idx_shop_operation_shop_id ON shop_operation;
CREATE INDEX idx_shop_operation_shop_id_open_deleted ON shop_operation(shop_id, is_open, is_deleted);
