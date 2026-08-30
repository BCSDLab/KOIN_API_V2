package in.koreatech.koin.domain.team.recruitment.enums;

/**
 * 지원 불가 사유. 여러 사유가 동시에 성립하면 선언 순서가 이른 값을 반환한다.
 * <p>
 * 순서는 클라이언트와 합의한 화면 안내 우선순위를 따른다.
 * 지원 API 의 검증 순서와는 일부 다르므로, 이미 지원한 마감 글처럼 두 사유가 겹치는 경우
 * 상세가 알려주는 사유와 실제 지원 시 오류 코드가 다를 수 있다.
 */
public enum TeamRecruitmentApplyBlockReason {
    /**
     * 상세 조회는 삭제된 모집글에 404 를 반환하므로 이 값은 상세 응답으로 나가지 않는다.
     */
    RECRUITMENT_DELETED,
    LOGIN_REQUIRED,
    OWN_RECRUITMENT,
    ALREADY_APPLIED,
    RECRUITMENT_CLOSED,
    DEADLINE_PASSED,
    ROLE_CLOSED,
    PROFILE_REQUIRED
}
