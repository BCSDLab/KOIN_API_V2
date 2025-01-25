ALTER TABLE students
    ADD COLUMN major_id INT UNSIGNED;

ALTER TABLE student
    ADD CONSTRAINT fk_student_major
        FOREIGN KEY (major_id) REFERENCES major(id)
            ON UPDATE CASCADE;