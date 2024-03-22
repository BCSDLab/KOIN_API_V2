CREATE TABLE `koin`.`dept_infos`
(
    `name`            varchar(45) COLLATE utf8mb4_bin  NOT NULL,
    `curriculum_link` varchar(255) COLLATE utf8mb4_bin NOT NULL,
    `is_deleted`      tinyint(1) NOT NULL DEFAULT '0',
    PRIMARY KEY (`name`),
    UNIQUE KEY `dept_name_UNIQUE` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

CREATE TABLE `koin`.`dept_nums`
(
    `dept_name` varchar(45) COLLATE utf8mb4_bin NOT NULL,
    `dept_num`  varchar(5) COLLATE utf8mb4_bin  NOT NULL,
    PRIMARY KEY (`dept_name`, `dept_num`),
    KEY         `idx_dept_num` (`dept_num`),
    CONSTRAINT `fk_dept_name` FOREIGN KEY (`dept_name`) REFERENCES `dept_infos` (`name`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

INSERT INTO `koin`.`dept_infos` (`name`, `curriculum_link`)
VALUES ('기계공학부', 'https://cms3.koreatech.ac.kr/me/795/subview.do'),
       ('메카트로닉스공학부', 'https://www.koreatech.ac.kr/kor/CMS/UnivOrganMgr/subMain.do?mCode=MN076'),
       ('전기전자통신공학부', 'https://cms3.koreatech.ac.kr/ite/842/subview.do'),
       ('컴퓨터공학부', 'https://cse.koreatech.ac.kr/page_izgw21'),
       ('디자인공학부', 'https://cms3.koreatech.ac.kr/ide/1047/subview.do'),
       ('건축공학부', 'https://cms3.koreatech.ac.kr/arch/1083/subview.do'),
       ('에너지신소재화학공학부', 'https://cms3.koreatech.ac.kr/ace/992/subview.do'),
       ('산업경영학부', 'https://cms3.koreatech.ac.kr/sim/1167/subview.do'),
       ('고용서비스정책학과', 'https://www.koreatech.ac.kr/kor/CMS/UnivOrganMgr/subMain.do?mCode=MN451');


INSERT INTO `koin`.`dept_nums` (`dept_name`, `dept_num`)
VALUES ('기계공학부', '20'),
       ('메카트로닉스공학부', '40'),
       ('전기전자통신공학부', '61'),
       ('컴퓨터공학부', '35'),
       ('컴퓨터공학부', '36'),
       ('디자인공학부', '51'),
       ('건축공학부', '72'),
       ('에너지신소재화학공학부', '74'),
       ('산업경영학부', '80'),
       ('고용서비스정책학과', '85');
