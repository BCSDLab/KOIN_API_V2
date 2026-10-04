-- 모집글 제목과 이미 저장된 알림 문구를 각각 교정한다.
-- 다른 환경의 데이터나 이후 수정된 값은 덮어쓰지 않는다.
UPDATE `team_recruitment`
SET `title` = '공모전'
WHERE `id` = 71
    AND BINARY `title` = BINARY '공모저온';

UPDATE `team_recruitment_notification`
SET `message_preview` = '지원하신 공모전 모집이 마감되어 지원이 거절되었어요.'
WHERE `id` = 136
    AND `recruitment_id` = 71
    AND `application_id` = 52
    AND `type` = 'APPLICATION_REJECTED'
    AND `target_type` = 'MY_APPLICATIONS'
    AND BINARY `message_preview` = BINARY '지원하신 공모저온 모집이 마감되어 지원이 거절되었어요.';
