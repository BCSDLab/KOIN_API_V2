DELIMITER $$

CREATE PROCEDURE call_array_class_time_normalization(
    IN_SEMESTER_DATE VARCHAR(10)
)
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE lecture_id INT;
    DECLARE flag INT DEFAULT 0;

    -- 커서 선언
    DECLARE cur CURSOR FOR
    SELECT id FROM lectures
    where semester_date = IN_SEMESTER_DATE;

    -- 종료 핸들러 정의
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET flag = 1;

    -- 커서 열기
    OPEN cur;

    lecture_loop: LOOP
        FETCH cur INTO lecture_id; -- 여기서 반환값이 없으면 에러가 발생 -> 이를 종료 핸들러가 캐치
        IF flag THEN               -- 데이터가 없으면 루프 종료
            LEAVE lecture_loop;
    END IF;

    CALL array_class_time_normalization(lecture_id);
END LOOP;

    -- 커서 닫기
    CLOSE cur;
END$$

DELIMITER ;

-- 2019
CALL call_array_class_time_normalization("20191");
CALL call_array_class_time_normalization("2019-여름");
CALL call_array_class_time_normalization("20192");
CALL call_array_class_time_normalization("2019-겨울");

-- 2020
CALL call_array_class_time_normalization("20201");
CALL call_array_class_time_normalization("2020-여름");
CALL call_array_class_time_normalization("20202");
CALL call_array_class_time_normalization("2020-겨울");

-- 2021
CALL call_array_class_time_normalization("20211");
CALL call_array_class_time_normalization("2021-여름");
CALL call_array_class_time_normalization("20212");
CALL call_array_class_time_normalization("2021-겨울");

-- 2022
CALL call_array_class_time_normalization("20221");
CALL call_array_class_time_normalization("2022-여름");
CALL call_array_class_time_normalization("20222");
CALL call_array_class_time_normalization("2022-겨울");

-- 2023
CALL call_array_class_time_normalization("20231");
CALL call_array_class_time_normalization("2023-여름");
CALL call_array_class_time_normalization("20232");
CALL call_array_class_time_normalization("2023-겨울");

-- 2024
CALL call_array_class_time_normalization("20241");
CALL call_array_class_time_normalization("2024-여름");
CALL call_array_class_time_normalization("20242");
CALL call_array_class_time_normalization("2024-겨울");
