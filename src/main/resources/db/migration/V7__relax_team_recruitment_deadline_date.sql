-- Immutable follow-up migration.
-- 지원 마감일과 활동 시작일의 순서 제한을 제거한다. 활동 시작 이후까지 지원을 받는 모집글이 있어
-- 마감일은 활동 기간과 무관하게 받는다. 활동 시작일 <= 활동 종료일 제약은 그대로 유지한다.
ALTER TABLE `team_recruitment`
    DROP CHECK `chk_team_recruitment_dates`;

ALTER TABLE `team_recruitment`
    ADD CONSTRAINT `chk_team_recruitment_dates`
        CHECK (`activity_start_date` <= `activity_end_date`);
