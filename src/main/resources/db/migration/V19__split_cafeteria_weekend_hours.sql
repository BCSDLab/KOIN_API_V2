-- 개별 요일을 판정할 수 있는 서버를 먼저 배포한 뒤 적용한다.
-- 현재 적용 중인 학기와 학생식당이 각각 하나일 때만 데이터를 이행한다.
SET @v19_shop_id = NULL, @v19_semester_id = NULL;

SELECT shop.id, semester.id INTO @v19_shop_id, @v19_semester_id
FROM coop_shop shop
JOIN coop_names name ON name.id = shop.coop_name_id
JOIN coop_semester semester ON semester.id = shop.semester_id
WHERE name.name = '학생식당'
  AND semester.is_applied = 1
  AND (SELECT COUNT(*) FROM coop_semester WHERE is_applied = 1) = 1
  AND (
      SELECT COUNT(*)
      FROM coop_shop candidate
      JOIN coop_names candidate_name ON candidate_name.id = candidate.coop_name_id
      WHERE candidate.semester_id = semester.id
        AND candidate_name.name = '학생식당'
  ) = 1
FOR UPDATE;

SELECT id FROM coop_opens WHERE coop_shop_id = @v19_shop_id FOR UPDATE;

SELECT
    COUNT(CASE WHEN day_of_week = 'WEEKEND' THEN 1 END),
    COUNT(DISTINCT CASE WHEN day_of_week = 'WEEKEND' AND type IN ('아침', '점심', '저녁') THEN type END),
    COUNT(CASE WHEN day_of_week = 'SATURDAY' THEN 1 END),
    COUNT(DISTINCT CASE WHEN day_of_week = 'SATURDAY' AND type IN ('아침', '점심', '저녁') THEN type END),
    COUNT(CASE WHEN day_of_week = 'SUNDAY' THEN 1 END)
INTO @v19_weekend_count, @v19_weekend_meals, @v19_saturday_count, @v19_saturday_meals, @v19_sunday_count
FROM coop_opens
WHERE coop_shop_id = @v19_shop_id;

-- 중복 또는 혼합된 시간표는 유지하고, 아침/점심/저녁이 있는 주말 또는 토요일 시간표만 처리한다.
SET @v19_can_migrate = @v19_shop_id IS NOT NULL AND (
    (@v19_weekend_count = 3 AND @v19_weekend_meals = 3 AND @v19_saturday_count = 0)
    OR (@v19_weekend_count = 0 AND @v19_saturday_count = 3 AND @v19_saturday_meals = 3)
);

UPDATE coop_opens
SET day_of_week = 'SATURDAY', updated_at = CURRENT_TIMESTAMP
WHERE coop_shop_id = @v19_shop_id
  AND day_of_week = 'WEEKEND'
  AND @v19_can_migrate;

SET @v19_changed_rows = ROW_COUNT();

-- 이미 등록된 일요일 시간표는 보존한다. 시험기간 운영시간은 기존 관리 경로에서 입력한다.
INSERT INTO coop_opens (coop_shop_id, type, day_of_week, open_time, close_time)
SELECT @v19_shop_id, meal.type, 'SUNDAY', '미운영', '미운영'
FROM (
    SELECT '아침' AS type
    UNION ALL SELECT '점심'
    UNION ALL SELECT '저녁'
) meal
WHERE @v19_can_migrate AND @v19_sunday_count = 0;

SET @v19_changed_rows = @v19_changed_rows + ROW_COUNT();

UPDATE coop_shop
SET updated_at = CURRENT_TIMESTAMP
WHERE id = @v19_shop_id AND @v19_changed_rows > 0;

UPDATE coop_semester
SET updated_at = CURRENT_TIMESTAMP
WHERE id = @v19_semester_id AND @v19_changed_rows > 0;
