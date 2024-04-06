ALTER TABLE `dining_menus`
    MODIFY COLUMN sold_out BOOLEAN NULL,
    MODIFY COLUMN is_changed BOOLEAN NULL;

UPDATE `dining_menus`
SET sold_out = NULL,
    is_changed = NULL;

ALTER TABLE `dining_menus`
    MODIFY COLUMN sold_out DATETIME NULL,
    MODIFY COLUMN is_changed DATETIME NULL;
