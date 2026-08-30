package in.koreatech.koin.domain.team.recruitment.dto;

import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_START_DATE_AFTER_END_DATE;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_INVALID_DEADLINE_DATE;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_INVALID_ROLE_COMPOSITION;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Predicate;

import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType;
import in.koreatech.koin.global.exception.CustomException;

/**
 * 모집글 작성/수정 요청이 공통으로 쓰는 검증. null 필드는 Bean Validation 이 처리하므로 여기서는 건너뛴다.
 */
final class RecruitmentRequestValidator {

    static final int MAX_ROLE_COUNT = 5;
    static final int MAX_GENERAL_PARTICIPANTS = 10;

    // 전체 모집 정원 상한. team_recruitment.max_participants CHECK 도 1~10 이다.
    static final int MAX_TOTAL_PARTICIPANTS = 10;

    private RecruitmentRequestValidator() {
    }

    static void validatePeriod(LocalDate activityStartDate, LocalDate activityEndDate, LocalDate deadlineDate) {
        if (activityStartDate == null) {
            return;
        }
        if (activityEndDate != null && activityEndDate.isBefore(activityStartDate)) {
            throw CustomException.of(INVALID_START_DATE_AFTER_END_DATE);
        }
        if (deadlineDate != null && deadlineDate.isAfter(activityStartDate)) {
            throw CustomException.of(TEAM_RECRUITMENT_INVALID_DEADLINE_DATE);
        }
    }

    /**
     * 전체 모집 정원은 최대 10명이다.
     * ROLE_BASED 는 역할 정원의 합이 전체 정원이 되므로 합계도 같은 상한을 지켜야 한다.
     */
    static void validateTotalCapacity(int totalCapacity) {
        if (totalCapacity > MAX_TOTAL_PARTICIPANTS) {
            throw CustomException.of(INVALID_REQUEST_BODY, "totalCapacity: " + totalCapacity);
        }
    }

    /**
     * 역할명은 앞뒤 공백을 제거한 형태를 정본으로 삼는다.
     * utf8mb4_0900_ai_ci 는 NO PAD 라 DB 에서는 "PM" 과 "PM " 이 다르지만,
     * 사용자에게는 구분되지 않는 이름이므로 저장 값 자체를 공백 없이 통일한다.
     */
    static String canonicalRoleName(String roleName) {
        return roleName == null ? null : roleName.trim();
    }

    /**
     * 역할명에는 (recruitment_id, name) unique 제약이 걸려 있고 collation 이 utf8mb4_0900_ai_ci 라
     * 대소문자와 악센트를 무시한 채 같으면 중복으로 본다. 요청 단계에서 걸러 DB 오류를 막는다.
     * 이름은 canonicalRoleName 으로 이미 공백이 제거된 상태로 들어온다.
     */
    static void validateDistinctRoleNames(List<String> roleNames) {
        Set<String> normalized = new HashSet<>();
        for (String roleName : roleNames) {
            if (roleName != null && !normalized.add(normalizeRoleName(roleName))) {
                throw CustomException.of(INVALID_REQUEST_BODY, "duplicated roleName: " + roleName);
            }
        }
    }

    private static String normalizeRoleName(String roleName) {
        String withoutMarks = Normalizer.normalize(roleName, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "");
        return withoutMarks.toLowerCase(Locale.ROOT);
    }

    /**
     * 압축 생성자는 Bean Validation 보다 먼저 실행되므로, 역할 목록에 null 이 섞여 있거나
     * 필수 값이 비어 있으면 교차 검증에서 NullPointerException 이 날 수 있다.
     * 그런 요청은 Bean Validation 이 400 으로 처리하므로 교차 검증을 건너뛴다.
     */
    static <T> boolean isCompleteRoleList(List<T> roles, Predicate<T> complete) {
        return roles != null && roles.stream().allMatch(role -> role != null && complete.test(role));
    }

    static void validateRoleComposition(
        TeamRecruitmentType recruitmentType,
        List<?> roles,
        Integer maxParticipants
    ) {
        if (recruitmentType == null || roles == null) {
            return;
        }
        if (recruitmentType == TeamRecruitmentType.ROLE_BASED) {
            if (roles.isEmpty() || roles.size() > MAX_ROLE_COUNT) {
                throw CustomException.of(TEAM_RECRUITMENT_INVALID_ROLE_COMPOSITION);
            }
            return;
        }
        boolean invalidGeneral = !roles.isEmpty()
            || maxParticipants == null
            || maxParticipants < 1
            || maxParticipants > MAX_GENERAL_PARTICIPANTS;
        if (invalidGeneral) {
            throw CustomException.of(TEAM_RECRUITMENT_INVALID_ROLE_COMPOSITION);
        }
    }
}
