-- 1. student 테이블에 department_id 컬럼 추가
ALTER TABLE student
    ADD COLUMN department_id INT UNSIGNED NOT NULL comment '학과 id';

-- 2. student 테이블의 기존 department 데이터를 department 테이블의 id로 매핑
UPDATE student s
    JOIN department d ON s.department = d.name
    SET s.department_id = d.id;

-- 3. 기존 department(varchar) 컬럼 삭제
ALTER TABLE student
DROP COLUMN department;

-- 4. department_id 컬럼을 외래 키로 변경
ALTER TABLE student
    ADD CONSTRAINT fk_student_department
        FOREIGN KEY (department_id) REFERENCES department(id);
