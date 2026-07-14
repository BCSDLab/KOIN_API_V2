CREATE TABLE `department_contacts` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `department` VARCHAR(100) NOT NULL,
    `task` VARCHAR(255) NOT NULL DEFAULT '',
    `phone_number` VARCHAR(20) NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_department_contacts_department` (`department`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `department_contacts` (`id`, `department`, `task`, `phone_number`)
VALUES
    (1, '전문대학원 교학팀', '', '041-521-8203'),
    (2, '대학원 교학팀', '', '041-560-2577'),
    (3, '미래교육지원팀', '', '041-580-4702'),
    (4, 'Edutech 센터', '', '041-580-4707'),
    (5, '교무팀', '', '041-560-2524'),
    (6, '기계공학부 학부사무실', '', '041-560-1290'),
    (7, '메카트로닉스공학부 학부사무실', '', '041-560-1376'),
    (8, '전전통 학부사무실', '', '041-560-1292'),
    (9, '컴퓨터공학부 학부사무실', '', '041-560-1461'),
    (10, '디자인건축공학부 학부사무실', '', '041-560-1221'),
    (11, '에신화 학부사무실', '', '041-560-1302'),
    (12, '경영학부 학부사무실', '', '041-560-1437'),
    (13, '고용 학부사무실', '', '041-560-1761'),
    (14, '교양학부', '', '041-560-1294'),
    (15, 'HRD학부', '', '041-560-1295'),
    (16, '미래융합학부', '', '041-560-1489'),
    (17, '학사팀', '학사팀 업무총괄', '041-560-2539'),
    (18, '학사팀', '교육과정', '041-560-2527'),
    (19, '학사팀', '수업', '041-560-2528'),
    (20, '학사팀', '학적', '041-560-2537'),
    (21, '학사팀', '성적', '041-560-2589'),
    (22, '학사팀', '졸업관리, 출석인정', '041-560-2526');
