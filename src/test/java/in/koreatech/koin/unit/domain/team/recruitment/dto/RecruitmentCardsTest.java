package in.koreatech.koin.unit.domain.team.recruitment.dto;

import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.CLOSED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.DELETED;
import static in.koreatech.koin.domain.team.recruitment.enums.TeamRecruitmentStatus.RECRUITING;
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
        assertThat(RecruitmentCards.dDayOf(RECRUITING, DEADLINE, DEADLINE.minusDays(8))).isEqualTo(8);
    }

    @Test
    @DisplayName("마감일 당일은 0을 반환한다")
    void returnsZeroOnDeadline() {
        assertThat(RecruitmentCards.dDayOf(RECRUITING, DEADLINE, DEADLINE)).isZero();
    }

    @Test
    @DisplayName("마감일이 지나면 null을 반환한다")
    void returnsNullAfterDeadline() {
        assertThat(RecruitmentCards.dDayOf(RECRUITING, DEADLINE, DEADLINE.plusDays(1))).isNull();
    }

    @Test
    @DisplayName("마감 상태가 CLOSED이면 미래 마감일이어도 null을 반환한다")
    void returnsNullForClosedRecruitment() {
        assertThat(RecruitmentCards.dDayOf(CLOSED, DEADLINE, DEADLINE.minusDays(8))).isNull();
    }

    @Test
    @DisplayName("마감 상태가 DELETED이면 미래 마감일이어도 null을 반환한다")
    void returnsNullForDeletedRecruitment() {
        assertThat(RecruitmentCards.dDayOf(DELETED, DEADLINE, DEADLINE.minusDays(8))).isNull();
    }

    @Test
    @DisplayName("마감일이 null이면 모집 중이어도 null을 반환한다")
    void returnsNullForNullDeadline() {
        assertThat(RecruitmentCards.dDayOf(RECRUITING, null, DEADLINE.minusDays(8))).isNull();
    }
}
