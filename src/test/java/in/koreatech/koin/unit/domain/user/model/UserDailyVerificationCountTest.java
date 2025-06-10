package in.koreatech.koin.unit.domain.user.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import in.koreatech.koin._common.exception.custom.TooManyRequestsException;
import in.koreatech.koin.domain.user.verification.model.UserDailyVerificationCount;

class UserDailyVerificationCountTest {

    private static final String TEST_IDENTITY = "test@koreatech.ac.kr";

    @Nested
    @DisplayName("객체 생성 테스트")
    class CreateTest {

        @Test
        @DisplayName("객체를 생성하면 인증 횟수는 1로, 유효시간은 24시간으로 초기화된다")
        void create() {
            // given
            long expectedExpiration = 60 * 60 * 24L;

            // when
            UserDailyVerificationCount dailyCount = UserDailyVerificationCount.create(TEST_IDENTITY);

            // then
            assertThat(dailyCount.getId()).isEqualTo(TEST_IDENTITY);
            assertThat(dailyCount.getVerificationCount()).isEqualTo(1);
            assertThat(dailyCount.getExpiration()).isEqualTo(expectedExpiration);
        }
    }

    @Nested
    @DisplayName("인증 횟수 증가 테스트")
    class IncrementTest {

        @Test
        @DisplayName("최초 생성 후 increment()를 호출하면 인증 횟수가 2가 된다")
        void incrementSuccess() {
            // given
            UserDailyVerificationCount dailyCount = UserDailyVerificationCount.create(TEST_IDENTITY);
            assertThat(dailyCount.getVerificationCount()).isEqualTo(1);

            // when
            dailyCount.increment();

            // then
            assertThat(dailyCount.getVerificationCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("최대 인증 횟수(5회)를 초과하여 요청하면 TooManyRequestsException이 발생한다")
        void incrementFail() {
            // given
            UserDailyVerificationCount dailyCount = UserDailyVerificationCount.create(TEST_IDENTITY);

            // when
            dailyCount.increment(); // count = 2
            dailyCount.increment(); // count = 3
            dailyCount.increment(); // count = 4
            dailyCount.increment(); // count = 5

            // then
            assertThatThrownBy(dailyCount::increment)
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessage("하루 인증 횟수를 초과했습니다.");
        }
    }
}