UPDATE `koin`.`students`
SET major_id = CASE
                   WHEN department_id = 1 THEN 1
                   WHEN department_id = 2 THEN 2
                   WHEN department_id = 3 THEN 3
                   WHEN department_id = 4 THEN 6
                   WHEN department_id = 5 THEN 9
                   WHEN department_id = 6 THEN 10
                   WHEN department_id = 7 THEN 11
                   WHEN department_id = 8 THEN 12
                   WHEN department_id = 12 THEN 13
                   WHEN department_id = 9 THEN 14
                   WHEN department_id = 10 THEN 18
                   ELSE major_id
    END
WHERE department_id IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12);
