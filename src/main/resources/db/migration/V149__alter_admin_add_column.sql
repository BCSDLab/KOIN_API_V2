-- 컬럼 추가
ALTER TABLE `koin`.`admins`
    ADD COLUMN `name`           VARCHAR(10) NULL COMMENT '이름',
    ADD COLUMN `email`          VARCHAR(100) NULL COMMENT '이메일',
    ADD COLUMN `phone_number`   VARCHAR(20) NULL COMMENT '전화번호',
    ADD COLUMN `role`           VARCHAR(20) NOT NULL DEFAULT 'TRACK_REGULAR' COMMENT '직위';

-- 데이터 이동
UPDATE `koin`.`admins`
JOIN users ON users.id = admins.user_id
SET
    admins.name = users.name,
    admins.email = users.email,
    admins.phone_number = users.phone_number;

ALTER TABLE `koin`.`admins`
    MODIFY COLUMN `name`         VARCHAR(10)    NOT NULL COMMENT '이름',
    MODIFY COLUMN `email`        VARCHAR(100)   NOT NULL COMMENT '이메일',
    MODIFY COLUMN `phone_number` VARCHAR(20)    NOT NULL COMMENT '전화번호';
