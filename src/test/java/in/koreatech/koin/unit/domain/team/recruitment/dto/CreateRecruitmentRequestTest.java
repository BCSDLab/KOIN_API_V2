package in.koreatech.koin.unit.domain.team.recruitment.dto;

import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_REQUEST_BODY;
import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_START_DATE_AFTER_END_DATE;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_INVALID_DEADLINE_DATE;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_INVALID_ROLE_COMPOSITION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.team.recruitment.dto.CreateRecruitmentRequest;
import in.koreatech.koin.domain.team.recruitment.dto.RoleInput;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentCategory;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentMeetingType;
import in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentType;
import in.koreatech.koin.global.exception.CustomException;

class CreateRecruitmentRequestTest {

    private static final LocalDate DEADLINE = LocalDate.of(2026, 9, 3);
    private static final LocalDate ACTIVITY_START = LocalDate.of(2026, 9, 7);
    private static final LocalDate ACTIVITY_END = LocalDate.of(2026, 9, 30);

    private static CreateRecruitmentRequest create(
        TeamRecruitmentType recruitmentType,
        Integer maxParticipants,
        List<RoleInput> roles,
        LocalDate activityStartDate,
        LocalDate activityEndDate,
        LocalDate deadlineDate
    ) {
        return new CreateRecruitmentRequest(
            TeamRecruitmentCategory.CONTEST,
            "AI 아이디어 공모전 팀원 모집",
            TeamRecruitmentMeetingType.ONLINE,
            activityStartDate,
            activityEndDate,
            deadlineDate,
            recruitmentType,
            maxParticipants,
            roles,
            "공모전 팀원을 모집합니다.",
            null,
            null
        );
    }

    private static CreateRecruitmentRequest roleBased(List<RoleInput> roles) {
        return create(TeamRecruitmentType.ROLE_BASED, null, roles, ACTIVITY_START, ACTIVITY_END, DEADLINE);
    }

    private static CreateRecruitmentRequest general(Integer maxParticipants, List<RoleInput> roles) {
        return create(TeamRecruitmentType.GENERAL, maxParticipants, roles, ACTIVITY_START, ACTIVITY_END, DEADLINE);
    }

    @Nested
    @DisplayName("역할 구성 검증")
    class RoleComposition {

        @Test
        @DisplayName("ROLE_BASED 모집의 전체 정원은 역할 정원의 합이다")
        void roleBasedMaxParticipantsIsSumOfRoles() {
            CreateRecruitmentRequest request = roleBased(List.of(
                new RoleInput("PM", 1),
                new RoleInput("프론트엔드", 2)
            ));

            assertThat(request.resolveMaxParticipants()).isEqualTo(3);
        }

        @Test
        @DisplayName("GENERAL 모집의 전체 정원은 요청값을 그대로 사용한다")
        void generalMaxParticipantsIsRequestValue() {
            assertThat(general(5, List.of()).resolveMaxParticipants()).isEqualTo(5);
        }

        @Test
        @DisplayName("ROLE_BASED 모집은 역할이 5개까지 허용된다")
        void roleBasedAllowsFiveRoles() {
            List<RoleInput> roles = IntStream.rangeClosed(1, 5)
                .mapToObj(index -> new RoleInput("역할" + index, 1))
                .toList();

            assertThatCode(() -> roleBased(roles)).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("ROLE_BASED 모집에 역할이 없으면 예외가 발생한다")
        void roleBasedRequiresAtLeastOneRole() {
            assertThatThrownBy(() -> roleBased(List.of()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_INVALID_ROLE_COMPOSITION);
        }

        @Test
        @DisplayName("ROLE_BASED 모집의 역할이 5개를 넘으면 예외가 발생한다")
        void roleBasedRejectsMoreThanFiveRoles() {
            List<RoleInput> roles = IntStream.rangeClosed(1, 6)
                .mapToObj(index -> new RoleInput("역할" + index, 1))
                .toList();

            assertThatThrownBy(() -> roleBased(roles))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_INVALID_ROLE_COMPOSITION);
        }

        @Test
        @DisplayName("GENERAL 모집에 역할이 있으면 예외가 발생한다")
        void generalRejectsRoles() {
            assertThatThrownBy(() -> general(5, List.of(new RoleInput("PM", 1))))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_INVALID_ROLE_COMPOSITION);
        }

        @Test
        @DisplayName("GENERAL 모집에 전체 정원이 없으면 예외가 발생한다")
        void generalRequiresMaxParticipants() {
            assertThatThrownBy(() -> general(null, List.of()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_INVALID_ROLE_COMPOSITION);
        }

        @Test
        @DisplayName("GENERAL 모집의 전체 정원이 10을 넘으면 예외가 발생한다")
        void generalRejectsMaxParticipantsOverLimit() {
            assertThatThrownBy(() -> general(11, List.of()))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_INVALID_ROLE_COMPOSITION);
        }
    }

    @Nested
    @DisplayName("전체 정원과 역할명 중복 검증")
    class CapacityAndDuplicateNames {

        @Test
        @DisplayName("역할 정원의 합이 10이면 통과한다")
        void allowsTotalCapacityOfTen() {
            assertThatCode(() -> roleBased(List.of(
                new RoleInput("Backend", 5),
                new RoleInput("Frontend", 5))))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("역할 정원의 합이 10을 넘으면 예외가 발생한다")
        void rejectsTotalCapacityOverTen() {
            assertThatThrownBy(() -> roleBased(List.of(
                new RoleInput("Backend", 6),
                new RoleInput("Frontend", 6))))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", INVALID_REQUEST_BODY);
        }

        @Test
        @DisplayName("역할명이 같으면 예외가 발생한다")
        void rejectsDuplicateRoleNames() {
            assertThatThrownBy(() -> roleBased(List.of(
                new RoleInput("PM", 1),
                new RoleInput("PM", 1))))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", INVALID_REQUEST_BODY);
        }

        @Test
        @DisplayName("역할명이 대소문자만 다르면 DB collation 기준으로 중복이다")
        void rejectsRoleNamesDifferingOnlyByCase() {
            assertThatThrownBy(() -> roleBased(List.of(
                new RoleInput("PM", 1),
                new RoleInput("pm", 1))))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", INVALID_REQUEST_BODY);
        }

        @Test
        @DisplayName("역할명의 앞뒤 공백은 제거되어 저장된다")
        void trimsRoleName() {
            CreateRecruitmentRequest request = roleBased(List.of(new RoleInput("  PM  ", 1)));

            assertThat(request.roles().get(0).name()).isEqualTo("PM");
        }

        @Test
        @DisplayName("앞뒤 공백만 다른 역할명은 중복이다")
        void rejectsRoleNamesDifferingOnlyBySurroundingSpace() {
            assertThatThrownBy(() -> roleBased(List.of(
                new RoleInput("PM", 1),
                new RoleInput("PM ", 1))))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", INVALID_REQUEST_BODY);
        }

        @Test
        @DisplayName("역할명이 악센트만 다르면 DB collation 기준으로 중복이다")
        void rejectsRoleNamesDifferingOnlyByAccent() {
            assertThatThrownBy(() -> roleBased(List.of(
                new RoleInput("Cafe", 1),
                new RoleInput("Café", 1))))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", INVALID_REQUEST_BODY);
        }
    }

    @Nested
    @DisplayName("기간 검증")
    class Period {

        private static final List<RoleInput> ROLES = List.of(new RoleInput("PM", 1));

        @Test
        @DisplayName("지원 마감일이 활동 시작일과 같아도 된다")
        void deadlineCanEqualActivityStartDate() {
            assertThatCode(() -> create(
                TeamRecruitmentType.ROLE_BASED, null, ROLES, ACTIVITY_START, ACTIVITY_END, ACTIVITY_START))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("활동 시작일과 종료일이 같아도 된다")
        void activityStartCanEqualActivityEnd() {
            assertThatCode(() -> create(
                TeamRecruitmentType.ROLE_BASED, null, ROLES, ACTIVITY_START, ACTIVITY_START, DEADLINE))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("지원 마감일이 활동 시작일보다 이후이면 예외가 발생한다")
        void deadlineAfterActivityStartDateFails() {
            assertThatThrownBy(() -> create(
                TeamRecruitmentType.ROLE_BASED, null, ROLES, ACTIVITY_START, ACTIVITY_END, ACTIVITY_START.plusDays(1)))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_INVALID_DEADLINE_DATE);
        }

        @Test
        @DisplayName("활동 종료일이 활동 시작일보다 이전이면 예외가 발생한다")
        void activityEndBeforeActivityStartFails() {
            assertThatThrownBy(() -> create(
                TeamRecruitmentType.ROLE_BASED, null, ROLES, ACTIVITY_START, ACTIVITY_START.minusDays(1), DEADLINE))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", INVALID_START_DATE_AFTER_END_DATE);
        }
    }
}
