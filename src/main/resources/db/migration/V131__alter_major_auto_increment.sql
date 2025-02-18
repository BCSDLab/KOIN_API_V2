ALTER TABLE koin.major AUTO_INCREMENT = 1;

ALTER TABLE koin.major
    MODIFY COLUMN name VARCHAR(255) NULL COMMENT '전공 이름';

ALTER TABLE koin.major
    ADD CONSTRAINT `unique_name_department` UNIQUE (`name`, `department_id`);

