-- 컬럼 추가
ALTER TABLE `koin`.`admins`
    ADD COLUMN `name`           VARCHAR(10) NULL COMMENT '이름';

-- 데이터 이동
UPDATE `koin`.`admins`
    JOIN users ON users.id = admins.user_id
    SET
        admins.name = users.name;

ALTER TABLE `koin`.`admins`
    MODIFY COLUMN `name`         VARCHAR(10)    NOT NULL COMMENT '이름';
