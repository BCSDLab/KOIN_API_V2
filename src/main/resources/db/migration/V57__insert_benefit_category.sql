INSERT INTO shop_benefit_categories (title, detail) VALUES
('배달비 아끼기', '계좌이체하면 배달비가 무료(할인)인 상점들을 모아뒀어요.'),
('최소주문금액 무료', '계좌이체하면 최소주문금액이 무료인 상점들을 모아뒀어요.'),
('서비스 증정', '계좌이체하면 서비스를 주는 상점들을 모아뒀어요.'),
('가게까지 픽업', '사장님께서 직접 가게까지 데려다주시는 상점들을 모아뒀어요.');

INSERT INTO shop_benefit_category_map (shop_id, benefit_id) VALUES
(11, 1),
(11, 1),
(13, 2),
(13, 3),
(11, 3),
(11, 3),
(13, 4);
