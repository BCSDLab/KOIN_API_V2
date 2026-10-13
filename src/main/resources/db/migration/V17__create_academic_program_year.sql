CREATE TABLE IF NOT EXISTS `academic_program_year` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `student_year` VARCHAR(4) NOT NULL COMMENT '학번 앞 네 자리',
    `department_id` INT UNSIGNED NOT NULL,
    `major_id` INT UNSIGNED DEFAULT NULL,
    `major_key` INT UNSIGNED GENERATED ALWAYS AS (IFNULL(`major_id`, 0)) STORED,
    PRIMARY KEY (`id`),
    UNIQUE KEY `ux_academic_program_year` (`student_year`, `department_id`, `major_key`),
    KEY `idx_academic_program_year_major` (`major_id`),
    CONSTRAINT `fk_academic_program_year_department`
        FOREIGN KEY (`department_id`) REFERENCES `department` (`id`),
    CONSTRAINT `fk_academic_program_year_major`
        FOREIGN KEY (`major_id`) REFERENCES `major` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
