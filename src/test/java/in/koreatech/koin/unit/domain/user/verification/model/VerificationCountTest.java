package in.koreatech.koin.unit.domain.user.verification.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin._common.exception.errorcode.ErrorCode;
import in.koreatech.koin.domain.user.verification.config.VerificationProperties;
import in.koreatech.koin.domain.user.verification.model.VerificationCount;
import in.koreatech.koin.unit.fixutre.VerificationFixture;

class VerificationCountTest {

    private VerificationCount SMS_인증_횟수;
    private VerificationCount 이메일_인증_횟수;
    private VerificationProperties verificationProperties;

    private static final String TEST_PHONE_NUMBER = "01012345678";
    private static final String TEST_EMAIL = "user@koreatech.ac.kr";

    @BeforeEach
    void init() {
        SMS_인증_횟수 = VerificationFixture.SMS_인증_횟수(TEST_PHONE_NUMBER);
        이메일_인증_횟수 = VerificationFixture.Email_인증_횟수(TEST_EMAIL);
        verificationProperties = new VerificationProperties(5);
    }

    @Nested
    class create {

        @Test
        void SMS_인증_횟수를_생성한다() {
            // given
            long expectedExpiration = 60 * 60 * 24L;
            // when
            VerificationCount dailyCount = VerificationCount.of(TEST_PHONE_NUMBER, verificationProperties);
            // then
            assertThat(dailyCount.getId()).isEqualTo(TEST_PHONE_NUMBER);
            assertThat(dailyCount.getVerificationCount()).isEqualTo(0);
            assertThat(dailyCount.getExpiration()).isEqualTo(expectedExpiration);
        }

        @Test
        void Email_인증_횟수를_생성한다() {
            // given
            long expectedExpiration = 60 * 60 * 24L;
            // when
            VerificationCount dailyCount = VerificationCount.of(TEST_EMAIL, verificationProperties);
            // then
            assertThat(dailyCount.getId()).isEqualTo(TEST_EMAIL);
            assertThat(dailyCount.getVerificationCount()).isEqualTo(0);
            assertThat(dailyCount.getExpiration()).isEqualTo(expectedExpiration);
        }
    }

    @Nested
    class increment {

        @Test
        void SMS_인증_횟수가_증가한다() {
            // when
            SMS_인증_횟수.incrementVerificationCount();
            // then
            assertEquals(1, SMS_인증_횟수.getVerificationCount());
        }

        @Test
        void Email_인증_횟수가_증가한다() {
            // when
            이메일_인증_횟수.incrementVerificationCount();
            // then
            assertEquals(1, 이메일_인증_횟수.getVerificationCount());
        }

        @Test
        void SMS_최대_인증_횟수를_초과하면_예외를_던진다() {
            // when
            Stream.generate(() -> SMS_인증_횟수)
                .limit(SMS_인증_횟수.getMaxVerificationCount())
                .forEach(VerificationCount::incrementVerificationCount);
            // then
            CustomException exception = assertThrows(CustomException.class, SMS_인증_횟수::incrementVerificationCount);
            assertEquals(ErrorCode.TOO_MANY_REQUESTS_VERIFICATION, exception.getErrorCode());
        }

        @Test
        void Email_최대_인증_횟수를_초과하면_예외를_던진다() {
            // when
            IntStream.rangeClosed(1, 5).forEach(i -> 이메일_인증_횟수.incrementVerificationCount());
            // then
            CustomException exception = assertThrows(CustomException.class, 이메일_인증_횟수::incrementVerificationCount);
            assertEquals(ErrorCode.TOO_MANY_REQUESTS_VERIFICATION, exception.getErrorCode());
        }
    }
}
