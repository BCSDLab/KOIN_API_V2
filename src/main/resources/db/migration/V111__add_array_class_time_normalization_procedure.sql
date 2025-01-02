DELIMITER $$

CREATE PROCEDURE array_class_time_normalization (
    IN_LECTURE_ID int unsigned
)
BEGIN
	-- 변수 선언
	DECLARE IN_CLASS_TIME varchar(255);
	DECLARE current_class_time int;
    DECLARE prev_class_time int DEFAULT NULL;
    DECLARE start_class_time int DEFAULT NULL;
    DECLARE end_class_time int DEFAULT NULL;
    DECLARE idx int DEFAULT 1;
    DECLARE count int DEFAULT 1;

    -- class_time 가져오기
    SELECT class_time INTO IN_CLASS_TIME
    FROM lectures
    WHERE id = IN_LECTURE_ID;

    -- 대괄호 앞 뒤 삭제
    SET IN_CLASS_TIME = REPLACE(REPLACE(IN_CLASS_TIME, '[', ''), ']', '');

    -- 강의 시간이 있는 경우에만 로직 실행
    IF LENGTH(IN_CLASS_TIME) != 0 THEN
		-- 총 실행 횟수, (현재 길이 - 콤마를 없앴을 때의 길이 + 1 = 총 시간 개수)
		SET count = LENGTH(IN_CLASS_TIME) - LENGTH(REPLACE(IN_CLASS_TIME, ',', '')) + 1;
		WHILE idx <= count DO
			-- 인덱스에 해당하는 시간 가져오기
			SET current_class_time = CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(IN_CLASS_TIME, ',', idx), ',', -1) AS UNSIGNED);

			-- 초기 시작 시간 설정 및 연속 시간 체크
			IF prev_class_time IS NULL OR current_class_time != prev_class_time + 1 THEN
				IF start_class_time IS NOT NULL THEN
					INSERT INTO lecture_information (lecture_id, start_time, end_time) VALUES (IN_LECTURE_ID, start_class_time, end_class_time);
                END IF;
				SET start_class_time = current_class_time;
            END IF;

            -- 다음 시간 저장
			SET end_class_time = current_class_time;
			SET prev_class_time = current_class_time;
			SET idx = idx + 1;
        END WHILE;

		-- 마지막 범위 추가
		IF start_class_time IS NOT NULL THEN
			INSERT INTO lecture_information (lecture_id, start_time, end_time) VALUES (IN_LECTURE_ID, start_class_time, end_class_time);
        END IF;
    END IF;

END $$
DELIMITER ;
