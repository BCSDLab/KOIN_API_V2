ALTER TABLE koin.standard_graduation_requirements
DROP FOREIGN KEY standard_graduation_requirements_department_id_fk,
DROP COLUMN department_id,
ADD COLUMN major_id INT UNSIGNED NOT NULL COMMENT '전공 id',
ADD CONSTRAINT fk_major_id FOREIGN KEY (major_id) REFERENCES major (id);
