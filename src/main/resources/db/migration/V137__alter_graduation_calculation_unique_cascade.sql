ALTER TABLE student_course_calculation
    ADD CONSTRAINT unique_user_standard_graduation_requirements UNIQUE (user_id, standard_graduation_requirements_id);

ALTER TABLE detect_graduation_calculation
    ADD CONSTRAINT unique_user UNIQUE (user_id);
