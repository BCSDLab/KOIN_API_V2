-- 기존 신청은 이관 데이터와 공통 배달비를 추정하지 않고 보존한다.
-- 기존 신청은 반려 후 새 신청 규격으로 재신청해야 승격할 수 있다.
ALTER TABLE shop_order_service_requests
    ADD COLUMN delivery_tip INT UNSIGNED NULL COMMENT '공통 배달비',
    ADD COLUMN transfer_data JSON NULL COMMENT '신청 시 확정된 상점 이미지 및 메뉴 이관 데이터';
