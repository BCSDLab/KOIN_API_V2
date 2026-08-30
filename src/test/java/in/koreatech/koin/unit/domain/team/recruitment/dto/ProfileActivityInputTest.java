package in.koreatech.koin.unit.domain.team.recruitment.dto;

import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_START_DATE_AFTER_END_DATE;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_ACTIVITY_END_DATE_MUST_BE_NULL;
import static in.koreatech.koin.global.code.ApiResponseCode.TEAM_RECRUITMENT_ACTIVITY_END_DATE_REQUIRED;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.team.recruitment.dto.ProfileActivityInput;
import in.koreatech.koin.global.exception.CustomException;

class ProfileActivityInputTest {

    private static final LocalDate STARTED_AT = LocalDate.of(2025, 3, 3);
    private static final LocalDate ENDED_AT = LocalDate.of(2025, 5, 5);

    private static ProfileActivityInput create(LocalDate endedAt, Boolean isOngoing) {
        return new ProfileActivityInput("AI 공모전", STARTED_AT, endedAt, isOngoing, "기획 담당");
    }

    @Nested
    @DisplayName("활동 기간 검증 성공")
    class ValidateSuccess {

        @Test
        @DisplayName("진행 중인 활동은 종료일이 없어도 된다")
        void ongoingActivityWithoutEndDate() {
            assertThatCode(() -> create(null, true))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("종료된 활동은 종료일이 시작일보다 이후이면 된다")
        void endedActivityWithEndDateAfterStartDate() {
            assertThatCode(() -> create(ENDED_AT, false))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("하루짜리 활동은 종료일이 시작일과 같아도 된다")
        void endedActivityWithEndDateEqualToStartDate() {
            assertThatCode(() -> create(STARTED_AT, false))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("활동 기간 검증 실패")
    class ValidateFail {

        @Test
        @DisplayName("진행 중인 활동에 종료일을 보내면 예외가 발생한다")
        void ongoingActivityWithEndDate() {
            assertThatThrownBy(() -> create(ENDED_AT, true))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_ACTIVITY_END_DATE_MUST_BE_NULL);
        }

        @Test
        @DisplayName("종료된 활동에 종료일이 없으면 예외가 발생한다")
        void endedActivityWithoutEndDate() {
            assertThatThrownBy(() -> create(null, false))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", TEAM_RECRUITMENT_ACTIVITY_END_DATE_REQUIRED);
        }

        @Test
        @DisplayName("종료일이 시작일보다 이전이면 예외가 발생한다")
        void endedActivityWithEndDateBeforeStartDate() {
            assertThatThrownBy(() -> create(STARTED_AT.minusDays(1), false))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", INVALID_START_DATE_AFTER_END_DATE);
        }
    }
}
