-- organizations 테이블 생성 (단체 정보 관리)
CREATE TABLE `organizations` (
    `id` INT NOT NULL AUTO_INCREMENT,
    `user_id` INT NOT NULL COMMENT '단체 계정 user_id',
    `name` VARCHAR(100) NOT NULL COMMENT '단체명 (예: 총학생회, 컴퓨터공학부)',
    `location` VARCHAR(255) NOT NULL COMMENT '방문 장소 (예: 학생회관 320호 총학생회 사무실)',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_organizations_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='단체 정보';

-- 초기 데이터 (총학생회) - user_id는 실제 총학생회 계정 ID로 변경 필요
-- INSERT INTO `organizations` (`user_id`, `name`, `location`) 
-- VALUES ({총학생회_user_id}, '총학생회', '학생회관 320호 총학생회 사무실로 방문');
