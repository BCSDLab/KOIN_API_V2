ALTER TABLE `department`
    ADD COLUMN `curriculum_link` VARCHAR(255) NOT NULL
        DEFAULT 'https://www.koreatech.ac.kr/menu.es?mid=a10201000000',
    ADD COLUMN `dept_nums` JSON NOT NULL DEFAULT (JSON_ARRAY('N/A')),
    ADD COLUMN `is_selectable` TINYINT(1) NOT NULL DEFAULT 1,
    ADD COLUMN `display_order` INT NOT NULL DEFAULT 1000;

-- 기존 /depts 응답의 링크, 학과 번호, 순서를 옮기며 학부 ID와 학적 연결은 유지한다.
UPDATE `department`
SET `curriculum_link` = CASE `name`
        WHEN '건축공학부' THEN 'https://www.koreatech.ac.kr/menu.es?mid=b30202000000'
        WHEN '고용서비스정책학과' THEN 'https://www.koreatech.ac.kr/board.es?mid=b80302000000&bid=0150'
        WHEN '기계공학부' THEN 'https://www.koreatech.ac.kr/menu.es?mid=a70302000000'
        WHEN '디자인공학부' THEN 'https://www.koreatech.ac.kr/menu.es?mid=b20302000000'
        WHEN '메카트로닉스공학부' THEN 'https://www.koreatech.ac.kr/menu.es?mid=a80302010200'
        WHEN '산업경영학부' THEN 'https://www.koreatech.ac.kr/menu.es?mid=b60302010100'
        WHEN '전기전자통신공학부' THEN 'https://www.koreatech.ac.kr/menu.es?mid=a90302010200'
        WHEN '컴퓨터공학부' THEN 'https://www.koreatech.ac.kr/menu.es?mid=b10402000000'
        WHEN '화학생명공학부' THEN 'https://www.koreatech.ac.kr/board.es?mid=b50301000000&bid=0135'
        WHEN '에너지신소재공학부' THEN 'https://www.koreatech.ac.kr/board.es?mid=b40301000000&bid=0128'
        WHEN '에너지신소재화학공학부' THEN 'https://cms3.koreatech.ac.kr/ace/992/subview.do'
        ELSE `curriculum_link`
    END,
    `dept_nums` = CASE `name`
        WHEN '건축공학부' THEN JSON_ARRAY('72')
        WHEN '고용서비스정책학과' THEN JSON_ARRAY('85')
        WHEN '기계공학부' THEN JSON_ARRAY('20')
        WHEN '디자인공학부' THEN JSON_ARRAY('51')
        WHEN '메카트로닉스공학부' THEN JSON_ARRAY('40')
        WHEN '산업경영학부' THEN JSON_ARRAY('80')
        WHEN '전기전자통신공학부' THEN JSON_ARRAY('61')
        WHEN '컴퓨터공학부' THEN JSON_ARRAY('35', '36')
        WHEN '에너지신소재화학공학부' THEN JSON_ARRAY('74')
        ELSE `dept_nums`
    END,
    `display_order` = CASE `name`
        WHEN '건축공학부' THEN 1
        WHEN '고용서비스정책학과' THEN 2
        WHEN '기계공학부' THEN 3
        WHEN '디자인공학부' THEN 4
        WHEN '메카트로닉스공학부' THEN 5
        WHEN '산업경영학부' THEN 6
        WHEN '전기전자통신공학부' THEN 7
        WHEN '컴퓨터공학부' THEN 8
        WHEN '화학생명공학부' THEN 9
        WHEN '에너지신소재공학부' THEN 10
        WHEN '반도체·디스플레이공학과' THEN 11
        WHEN '미래융합학부' THEN 12
        WHEN '공학융합자율전공' THEN 13
        WHEN 'ICT융합자율전공' THEN 14
        WHEN '사회융합자율전공' THEN 15
        ELSE `display_order`
    END,
    `is_selectable` = CASE
        WHEN `name` = '학과공통' THEN 0
        ELSE `is_selectable`
    END,
    `updated_at` = `updated_at`;
