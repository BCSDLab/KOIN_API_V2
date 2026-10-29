-- 2025/2026 대학요람으로 확인한 연결만 보정한다. 기존 학부·전공 및 학적 행은 변경하지 않는다.
-- MySQL의 일반 DDL은 암묵적으로 커밋되므로 임시 테이블만 사용하고 DML을 명시적으로 커밋한다.
DROP TEMPORARY TABLE IF EXISTS tmp_catalog;
DROP TEMPORARY TABLE IF EXISTS tmp_catalog_guard;
DROP TEMPORARY TABLE IF EXISTS tmp_catalog_ids;
DROP TEMPORARY TABLE IF EXISTS tmp_catalog_remove;

CREATE TEMPORARY TABLE tmp_catalog (
    student_year VARCHAR(4) NOT NULL,
    department_name VARCHAR(255) NOT NULL,
    major_name VARCHAR(255),
    has_major TINYINT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
CREATE TEMPORARY TABLE tmp_catalog_guard (
    step VARCHAR(30) PRIMARY KEY,
    ok TINYINT NOT NULL CHECK (ok = 1)
) ENGINE=InnoDB;

-- has_major=0은 학부 연결, 1은 이름이 NULL인 경우에도 실제 전공 행을 연결한다.
INSERT INTO tmp_catalog VALUES
    ('2025', '기계공학부', NULL, 0),
    ('2025', '메카트로닉스공학부', NULL, 0),
    ('2025', '전기전자통신공학부', NULL, 0),
    ('2025', '컴퓨터공학부', NULL, 0),
    ('2025', '디자인·건축공학부', NULL, 0),
    ('2025', '에너지신소재화학공학부', NULL, 0),
    ('2025', '산업경영학부', NULL, 0),
    ('2025', '고용서비스정책학과', NULL, 0),
    ('2026', '기계공학부', NULL, 0),
    ('2026', '메카트로닉스공학부', NULL, 0),
    ('2026', '디자인공학과', NULL, 0),
    ('2026', '건축공학과', NULL, 0),
    ('2026', '에너지신소재화학공학부', NULL, 0),
    ('2026', '공학융합자율전공', NULL, 0),
    ('2026', '전기전자통신공학부', NULL, 0),
    ('2026', '컴퓨터공학부', NULL, 0),
    ('2026', 'ICT융합자율전공', NULL, 0),
    ('2026', '경영학부', NULL, 0),
    ('2026', '고용서비스정책학과', NULL, 0),
    ('2026', '사회융합자율전공', NULL, 0),
    ('2026', '미래융합학부', NULL, 0),
    ('2026', '글로벌인재학부', NULL, 0),
    ('2026', '반도체·디스플레이공학과', NULL, 0),
    ('2025', '메카트로닉스공학부', '생산시스템전공', 1),
    ('2025', '메카트로닉스공학부', '제어시스템전공', 1),
    ('2025', '메카트로닉스공학부', '디지털시스템전공', 1),
    ('2025', '전기전자통신공학부', '전기공학전공', 1),
    ('2025', '전기전자통신공학부', '전자공학전공', 1),
    ('2025', '전기전자통신공학부', '정보통신공학전공', 1),
    ('2025', '디자인·건축공학부', '디자인공학전공', 1),
    ('2025', '디자인·건축공학부', '건축공학전공', 1),
    ('2025', '에너지신소재화학공학부', '에너지신소재공학전공', 1),
    ('2025', '에너지신소재화학공학부', '화학생명공학전공', 1),
    ('2025', '산업경영학부', '융합경영전공', 1),
    ('2025', '산업경영학부', '데이터경영전공', 1),
    ('2026', '메카트로닉스공학부', '생산시스템전공', 1),
    ('2026', '메카트로닉스공학부', '제어시스템전공', 1),
    ('2026', '메카트로닉스공학부', '디지털시스템전공', 1),
    ('2026', '에너지신소재화학공학부', '에너지신소재공학전공', 1),
    ('2026', '에너지신소재화학공학부', '화학생명공학전공', 1),
    ('2026', '전기전자통신공학부', '전기공학전공', 1),
    ('2026', '전기전자통신공학부', '전자공학전공', 1),
    ('2026', '전기전자통신공학부', 'AI정보통신공학전공', 1),
    ('2026', '컴퓨터공학부', '컴퓨터공학전공', 1),
    ('2026', '컴퓨터공학부', 'AI·소프트웨어전공', 1),
    ('2026', '경영학부', '융합경영전공', 1),
    ('2026', '경영학부', '데이터경영전공', 1),
    ('2026', '글로벌인재학부', '기계공학전공', 1),
    ('2026', '글로벌인재학부', '컴퓨터공학전공', 1),
    ('2025', '컴퓨터공학부', NULL, 1),
    ('2025', '기계공학부', NULL, 1),
    ('2025', '고용서비스정책학과', NULL, 1),
    ('2026', '기계공학부', NULL, 1),
    ('2026', '고용서비스정책학과', NULL, 1),
    ('2026', '디자인공학과', NULL, 1),
    ('2026', '건축공학과', NULL, 1),
    ('2025', '기전융합공학과', NULL, 0),
    ('2025', '기계설계공학과', NULL, 0),
    ('2025', '강소기업경영학과', NULL, 0),
    ('2026', '기전융합공학과', NULL, 0),
    ('2026', '기계설계공학과', NULL, 0),
    ('2026', '강소기업경영학과', NULL, 0);

START TRANSACTION;
SELECT id FROM department ORDER BY id FOR UPDATE;
SELECT id FROM major ORDER BY id FOR UPDATE;
SELECT id FROM academic_program_year ORDER BY id FOR UPDATE;

-- 이름으로 ID를 결정할 수 없는 중복은 영구 변경 전에 중단한다.
INSERT INTO tmp_catalog_guard
SELECT 'departments_unambiguous', NOT EXISTS (
    SELECT name FROM department GROUP BY name HAVING COUNT(*) > 1
);
INSERT INTO tmp_catalog_guard
SELECT 'majors_unambiguous', NOT EXISTS (
    SELECT m.department_id, m.name
    FROM major m JOIN department d ON d.id = m.department_id
    JOIN tmp_catalog w ON w.department_name = d.name AND w.has_major = 1
        AND m.name <=> w.major_name
    GROUP BY m.department_id, m.name HAVING COUNT(DISTINCT m.id) > 1
);

INSERT INTO department (name)
SELECT DISTINCT w.department_name
FROM tmp_catalog w
WHERE NOT EXISTS (SELECT 1 FROM department d WHERE d.name = w.department_name);

INSERT INTO major (department_id, name)
SELECT DISTINCT d.id, w.major_name
FROM tmp_catalog w JOIN department d ON d.name = w.department_name
WHERE w.has_major = 1 AND NOT EXISTS (
    SELECT 1 FROM major m WHERE m.department_id = d.id AND m.name <=> w.major_name
);

CREATE TEMPORARY TABLE tmp_catalog_ids ENGINE=InnoDB AS
SELECT w.student_year, d.id AS department_id, m.id AS major_id
FROM tmp_catalog w
JOIN department d ON d.name = w.department_name
LEFT JOIN major m ON w.has_major = 1 AND m.department_id = d.id AND m.name <=> w.major_name
WHERE w.has_major = 0 OR m.id IS NOT NULL;
INSERT INTO tmp_catalog_guard
SELECT 'all_ids_resolved', (SELECT COUNT(*) FROM tmp_catalog_ids) = 62;

INSERT INTO academic_program_year (student_year, department_id, major_id)
SELECT w.student_year, w.department_id, w.major_id
FROM tmp_catalog_ids w
WHERE NOT EXISTS (
    SELECT 1 FROM academic_program_year a
    WHERE a.student_year = w.student_year AND a.department_id = w.department_id
        AND a.major_id <=> w.major_id
);
INSERT INTO tmp_catalog_guard
SELECT 'mappings_present', NOT EXISTS (
    SELECT 1 FROM tmp_catalog_ids w WHERE NOT EXISTS (
        SELECT 1 FROM academic_program_year a
        WHERE a.student_year = w.student_year AND a.department_id = w.department_id
            AND a.major_id <=> w.major_id
    )
);

-- 확인된 오연결만 삭제한다. 이 목록 밖의 연결과 다른 연도는 그대로 둔다.
CREATE TEMPORARY TABLE tmp_catalog_remove ENGINE=InnoDB AS
SELECT a.id, a.student_year, a.department_id, a.major_id
FROM academic_program_year a
JOIN department d ON d.id = a.department_id
LEFT JOIN major m ON m.id = a.major_id
WHERE (a.student_year = '2025' AND d.name IN (
    '미래융합학부','공학융합자율전공','ICT융합자율전공','사회융합자율전공',
    '반도체·디스플레이공학과','응용화학공학부',
    '디자인공학부','건축공학부','화학생명공학부','에너지신소재공학부'))
   OR (a.student_year = '2025' AND d.name = '산업경영학부'
       AND m.name IN ('산업경영전공','혁신경영전공'))
   OR (a.student_year = '2026' AND d.name = '응용화학공학부');

DELETE a FROM tmp_catalog_remove r
STRAIGHT_JOIN academic_program_year a ON a.id = r.id
    AND a.student_year = r.student_year AND a.department_id = r.department_id
    AND a.major_id <=> r.major_id;

COMMIT;
DROP TEMPORARY TABLE tmp_catalog_remove;
DROP TEMPORARY TABLE tmp_catalog_ids;
DROP TEMPORARY TABLE tmp_catalog_guard;
DROP TEMPORARY TABLE tmp_catalog;
