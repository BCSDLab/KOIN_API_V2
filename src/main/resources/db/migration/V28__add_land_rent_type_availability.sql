ALTER TABLE `lands`
    ADD COLUMN `is_monthly_available` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '월세 매물 여부 (운영자가 체크)',
    ADD COLUMN `is_charter_available` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '전세 매물 여부 (운영자가 체크)';

UPDATE `lands`
SET `is_monthly_available` = 1
WHERE `monthly_fee` REGEXP '[^[:space:]]';

UPDATE `lands`
SET `is_charter_available` = 1
WHERE `charter_fee` REGEXP '[^[:space:]]';
