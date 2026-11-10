ALTER TABLE `shop_order_service_requests`
    ADD COLUMN `account_holder` VARCHAR(50) NULL COMMENT '예금주',
    ADD COLUMN `menu_snapshot` JSON NULL COMMENT '신청 시점의 이관 메뉴 및 추가 옵션';
