package in.koreatech.koin.unit.domain.user.verification.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import in.koreatech.koin._common.exception.custom.TooManyRequestsException;
import in.koreatech.koin.domain.user.verification.model.UserDailyVerificationCount;
import in.koreatech.koin.unit.fixutre.UserDailyVerificationCountFixture;

class UserDailyVerificationCountTest {

    private UserDailyVerificationCount SMS_인증_횟수;
    private UserDailyVerificationCount 이메일_인증_횟수;

    private static final String TEST_PHONE_NUMBER = "01012345678";
    private static final String TEST_EMAIL = "user@koreatech.ac.kr";

    @BeforeEach
    void init() {
        SMS_인증_횟수 = UserDailyVerificationCountFixture.SMS_인증_횟수(TEST_PHONE_NUMBER);
        이메일_인증_횟수 = UserDailyVerificationCountFixture.이메일_인증_횟수(TEST_EMAIL);
    }

    @Nested
    class create {

        @Test
        void 휴대폰을_이용하여_인증_횟수_객체를_생성할_수_있다() {
            // given
            long expectedExpiration = 60 * 60 * 24L;

            // when
            UserDailyVerificationCount dailyCount = UserDailyVerificationCount.create(TEST_PHONE_NUMBER);

            // then
            assertThat(dailyCount.getId()).isEqualTo(TEST_PHONE_NUMBER);
            assertThat(dailyCount.getVerificationCount()).isEqualTo(0);
            assertThat(dailyCount.getExpiration()).isEqualTo(expectedExpiration);
        }

        @Test
        void 이메일을_이용하여_인증_횟수_객체를_생성할_수_있다() {
            // given
            long expectedExpiration = 60 * 60 * 24L;

            // when
            UserDailyVerificationCount dailyCount = UserDailyVerificationCount.create(TEST_EMAIL);

            // then
            assertThat(dailyCount.getId()).isEqualTo(TEST_EMAIL);
            assertThat(dailyCount.getVerificationCount()).isEqualTo(0);
            assertThat(dailyCount.getExpiration()).isEqualTo(expectedExpiration);
        }
    }

    @Nested
    class increment {

        @Test
        void SMS_인증_횟수_객체에_increment를_호출하면_인증_횟수가_증가한다() {
            // when
            SMS_인증_횟수.increment();

            // then
            assertThat(SMS_인증_횟수.getVerificationCount()).isEqualTo(1);
        }

        @Test
        void 이메일_인증_횟수_객체에_increment를_호출하면_인증_횟수가_증가한다() {
            // when
            이메일_인증_횟수.increment();

            // then
            assertThat(이메일_인증_횟수.getVerificationCount()).isEqualTo(1);
        }

        @Test
        void SMS_인증_횟수_객체에_최대_인증_횟수를_초과하여_요청하면_TooManyRequestsException이_발생한다() {
            // when
            SMS_인증_횟수.increment(); // count = 1
            SMS_인증_횟수.increment(); // count = 2
            SMS_인증_횟수.increment(); // count = 3
            SMS_인증_횟수.increment(); // count = 4
            SMS_인증_횟수.increment(); // count = 5

            // then
            assertThatThrownBy(SMS_인증_횟수::increment)
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessage("하루 인증 횟수를 초과했습니다.");
        }

        @Test
        void 이메일_인증_횟수_객체에_최대_인증_횟수를_초과하여_요청하면_TooManyRequestsException이_발생한다() {
            // when
            이메일_인증_횟수.increment(); // count = 1
            이메일_인증_횟수.increment(); // count = 2
            이메일_인증_횟수.increment(); // count = 3
            이메일_인증_횟수.increment(); // count = 4
            이메일_인증_횟수.increment(); // count = 5

            // then
            assertThatThrownBy(이메일_인증_횟수::increment)
                .isInstanceOf(TooManyRequestsException.class)
                .hasMessage("하루 인증 횟수를 초과했습니다.");
        }
    }
}
