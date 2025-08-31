package in.koreatech.koin.unit.domain.shop;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import in.koreatech.koin.domain.shop.model.event.EventArticle;

@ExtendWith(MockitoExtension.class)
public class EventArticleDurationTest {

    private Clock createClock(LocalDateTime dateTime) {
        return Clock.fixed(
            dateTime.atZone(ZoneId.systemDefault()).toInstant(),
            ZoneId.systemDefault()
        );
    }

    @Nested
    @DisplayName("isOngoing: 이벤트 진행 여부 판별")
    class IsOngoing {

        @Test
        @DisplayName("이벤트 기간 내에 현재 날짜가 포함되면 true를 반환한다")
        void ongoingEvent() {
            // given: "오늘"을 2024년 7월 25일로 설정
            LocalDateTime now = LocalDateTime.of(2024, 7, 25, 10, 0);
            Clock clock = createClock(now);

            EventArticle eventArticle = EventArticle.builder().build();
            LocalDate startDate = null;
            try {
                startDate = LocalDate.from(now.minusDays(1));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            LocalDate endDate = LocalDate.from(now.plusDays(1));
            ReflectionTestUtils.setField(eventArticle, "startDate", startDate);
            ReflectionTestUtils.setField(eventArticle, "endDate", endDate);

            // when
            boolean result = eventArticle.isOngoing(clock);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("현재 날짜가 이벤트 시작일과 같으면 true를 반환한다")
        void eventStartsToday() {
            // given: "오늘"을 2024년 7월 25일로 설정
            LocalDateTime now = LocalDateTime.of(2024, 7, 25, 0, 0);
            Clock clock = createClock(now);

            EventArticle eventArticle = EventArticle.builder().build();
            LocalDate startDate = LocalDate.from(now); // 오늘 시작
            LocalDate endDate = LocalDate.from(now.plusDays(5));
            ReflectionTestUtils.setField(eventArticle, "startDate", startDate);
            ReflectionTestUtils.setField(eventArticle, "endDate", endDate);

            // when
            boolean result = eventArticle.isOngoing(clock);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("현재 날짜가 이벤트 종료일과 같으면 true를 반환한다")
        void eventEndsToday() {
            // given: "오늘"을 2024년 7월 25일로 설정
            LocalDateTime now = LocalDateTime.of(2024, 7, 25, 23, 59);
            Clock clock = createClock(now);

            EventArticle eventArticle = EventArticle.builder().build();
            LocalDate startDate = LocalDate.from(now.minusDays(5));
            LocalDate endDate = LocalDate.from(now); // 오늘 종료
            ReflectionTestUtils.setField(eventArticle, "startDate", startDate);
            ReflectionTestUtils.setField(eventArticle, "endDate", endDate);

            // when
            boolean result = eventArticle.isOngoing(clock);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("이벤트 시작일 전이면 false를 반환한다")
        void eventNotStarted() {
            // given: "오늘"을 2024년 7월 25일로 설정
            LocalDateTime now = LocalDateTime.of(2024, 7, 25, 10, 0);
            Clock clock = createClock(now);

            EventArticle eventArticle = EventArticle.builder().build();
            LocalDate startDate = LocalDate.from(now.plusDays(1)); // 내일 시작
            LocalDate endDate = LocalDate.from(now.plusDays(5));
            ReflectionTestUtils.setField(eventArticle, "startDate", startDate);
            ReflectionTestUtils.setField(eventArticle, "endDate", endDate);

            // when
            boolean result = eventArticle.isOngoing(clock);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("이벤트 종료일이 지났으면 false를 반환한다")
        void eventEnded() {
            // given: "오늘"을 2024년 7월 25일로 설정
            LocalDateTime now = LocalDateTime.of(2024, 7, 25, 10, 0);
            Clock clock = createClock(now);

            EventArticle eventArticle = EventArticle.builder().build();
            LocalDate startDate = LocalDate.from(now.minusDays(5));
            LocalDate endDate = LocalDate.from(now.minusDays(1)); // 어제 종료
            ReflectionTestUtils.setField(eventArticle, "startDate", startDate);
            ReflectionTestUtils.setField(eventArticle, "endDate", endDate);

            // when
            boolean result = eventArticle.isOngoing(clock);

            // then
            assertThat(result).isFalse();
        }
    }
}
