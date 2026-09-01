package in.koreatech.koin.unit.domain.team.recruitment.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import in.koreatech.koin.domain.team.recruitment.dto.RecruitmentCards;

@DisplayName("D-day 계산")
class RecruitmentCardsTest {

    private static final LocalDate DEADLINE = LocalDate.of(2026, 9, 3);

    @Test
    @DisplayName("마감일까지 남은 일수를 반환한다")
    void returnsDaysUntilDeadline() {
        assertThat(RecruitmentCards.dDayOf(DEADLINE, DEADLINE.minusDays(8))).isEqualTo(8);
    }

    @Test
    @DisplayName("마감일 당일은 0을 반환한다")
    void returnsZeroOnDeadline() {
        assertThat(RecruitmentCards.dDayOf(DEADLINE, DEADLINE)).isZero();
    }

    @Test
    @DisplayName("마감일이 지나면 null을 반환한다")
    void returnsNullAfterDeadline() {
        assertThat(RecruitmentCards.dDayOf(DEADLINE, DEADLINE.plusDays(1))).isNull();
    }
}
