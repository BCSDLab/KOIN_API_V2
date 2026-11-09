ALTER TABLE `shop_order_service_requests`
    ADD COLUMN `rejection_reason` VARCHAR(500) NULL COMMENT '주문 서비스 신청 반려 사유';
