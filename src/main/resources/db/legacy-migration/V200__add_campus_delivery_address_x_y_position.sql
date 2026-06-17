ALTER TABLE `koin`.`campus_delivery_address`
    ADD COLUMN `latitude` DECIMAL(10, 8) NULL COMMENT '위도' AFTER `short_address`,
    ADD COLUMN `longitude` DECIMAL(11, 8) NULL COMMENT '경도' AFTER `latitude`;

UPDATE `koin`.`campus_delivery_address`
SET `latitude` = 36.76125794, `longitude` = 127.28372942
WHERE `short_address` = '101동(해울)';

UPDATE `koin`.`campus_delivery_address`
SET `latitude` = 36.76156794, `longitude` = 127.28315659
WHERE `short_address` = '102동(예지)';

UPDATE `koin`.`campus_delivery_address`
SET `latitude` = 36.76186385, `longitude` = 127.28376805
WHERE `short_address` = '103동(예솔)';

UPDATE `koin`.`campus_delivery_address`
SET `latitude` = 36.76242319, `longitude` = 127.28349572
WHERE `short_address` = '104동(다솔)';

UPDATE `koin`.`campus_delivery_address`
SET `latitude` = 36.76202833, `longitude` = 127.28281109
WHERE `short_address` = '105동(함지)';

UPDATE `koin`.`campus_delivery_address`
SET `latitude` = 36.76163337, `longitude` = 127.28216566
WHERE `short_address` = '106동(한울)';

UPDATE `koin`.`campus_delivery_address`
SET `latitude` = 36.76130110, `longitude` = 127.28168287
WHERE `short_address` = '201동(솔빛)';

UPDATE `koin`.`campus_delivery_address`
SET `latitude` = 36.76084201, `longitude` = 127.28147960
WHERE `short_address` = '202동(청솔)';

UPDATE `koin`.`campus_delivery_address`
SET `latitude` = 36.76049078, `longitude` = 127.28139432
WHERE `short_address` = '203동(IH)';

UPDATE `koin`.`campus_delivery_address`
SET `latitude` = 36.76015392, `longitude` = 127.28094511
WHERE `short_address` = '204동(은솔)';

UPDATE `koin`.`campus_delivery_address`
SET `latitude` = 36.76083418, `longitude` = 127.28098119
WHERE `short_address` = '205동(참빛)';

UPDATE `koin`.`campus_delivery_address`
SET `latitude` = 36.76548530, `longitude` = 127.28040455
WHERE `short_address` = '공학 1관';

UPDATE `koin`.`campus_delivery_address`
SET `latitude` = 36.76674321, `longitude` = 127.28194918
WHERE `short_address` = '공학 2관';

UPDATE `koin`.`campus_delivery_address`
SET `latitude` = 36.76483840, `longitude` = 127.27959579
WHERE `short_address` = '공학 3관';

UPDATE `koin`.`campus_delivery_address`
SET `latitude` = 36.76146765, `longitude` = 127.27984115
WHERE `short_address` = '공학4관 A동';

UPDATE `koin`.`campus_delivery_address`
SET `latitude` = 36.76174156, `longitude` = 127.28023413
WHERE `short_address` = '공학4관 B동';

UPDATE `koin`.`campus_delivery_address`
SET `latitude` = 36.76590843, `longitude` = 127.28247253
WHERE `short_address` = '담헌실학관';

UPDATE `koin`.`campus_delivery_address`
SET `latitude` = 36.76498642, `longitude` = 127.28178594
WHERE `short_address` = '인문경영관';

UPDATE `koin`.`campus_delivery_address`
SET `latitude` = 36.76282473, `longitude` = 127.28326760
WHERE `short_address` = '테니스장';

UPDATE `koin`.`campus_delivery_address`
SET `latitude` = 36.76302056, `longitude` = 127.28238914
WHERE `short_address` = '학생회관';

UPDATE `koin`.`campus_delivery_address`
SET `latitude` = 36.76362895, `longitude` = 127.28042579
WHERE `short_address` = '다산정보관';

UPDATE `koin`.`campus_delivery_address`
SET `latitude` = 36.76303229, `longitude` = 127.28124121
WHERE `short_address` = '복지관';
