ALTER TABLE shop_benefit_categories
    ADD COLUMN on_image_url VARCHAR(255) NULL COMMENT '혜택 카테고리 on 이미지 url',
    ADD COLUMN off_image_url VARCHAR(255) NULL COMMENT '혜택 카테고리 off 이미지 url';

UPDATE shop_benefit_categories
SET on_image_url  = 'https://stage-static.koreatech.in/shop/benefit/deliveryOn.png',
    off_image_url = 'https://stage-static.koreatech.in/shop/benefit/deliveryOff.png'
WHERE title = '배달비 아끼기';

UPDATE shop_benefit_categories
SET on_image_url  = 'https://stage-static.koreatech.in/shop/benefit/paidOn.png',
    off_image_url = 'https://stage-static.koreatech.in/shop/benefit/paidOff.png'
WHERE title = '최소주문금액 무료';

UPDATE shop_benefit_categories
SET on_image_url  = 'https://stage-static.koreatech.in/shop/benefit/serviceOn.png',
    off_image_url = 'https://stage-static.koreatech.in/shop/benefit/serviceOff.png'
WHERE title = '서비스 증정';

UPDATE shop_benefit_categories
SET on_image_url  = 'https://stage-static.koreatech.in/shop/benefit/pickUpOn.png',
    off_image_url = 'https://stage-static.koreatech.in/shop/benefit/pickUpOff.png'
WHERE title = '가게까지 픽업';

ALTER TABLE shop_benefit_categories
    MODIFY COLUMN on_image_url VARCHAR (255) NOT NULL,
    MODIFY COLUMN off_image_url VARCHAR (255) NOT NULL;
