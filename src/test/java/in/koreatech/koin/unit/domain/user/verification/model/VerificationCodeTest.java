package in.koreatech.koin.unit.domain.user.verification.model;

import static in.koreatech.koin.domain.user.verification.model.VerificationChannel.EMAIL;
import static in.koreatech.koin.domain.user.verification.model.VerificationChannel.SMS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin._common.exception.errorcode.ErrorCode;
import in.koreatech.koin._common.util.random.VerificationNumberGenerator;
import in.koreatech.koin.domain.user.verification.model.VerificationCode;
import in.koreatech.koin.unit.domain.user.verification.mock.StubVerificationNumberHolder;
import in.koreatech.koin.unit.fixutre.VerificationFixture;

class VerificationCodeTest {

    private VerificationCode SMS_인증_코드;
    private VerificationCode Email_인증_코드;

    private static final String TEST_EMAIL = "user@koreatech.ac.kr";
    private static final String TEST_PHONE_NUMBER = "01012345678";
    private static final String CORRECT_CODE = "123456";
    private static final String WRONG_CODE = "999999";
    private static final int ABNORMAL_USAGE_THRESHOLD = 10;

    @BeforeEach
    void init() {
        SMS_인증_코드 = VerificationFixture.SMS_인증_코드(TEST_PHONE_NUMBER, CORRECT_CODE);
        Email_인증_코드 = VerificationFixture.Email_인증_코드(TEST_EMAIL, CORRECT_CODE);
    }

    @Nested
    class create {

        @Test
        void SMS_인증_코드를_생성한다() {
            // given
            VerificationNumberGenerator verificationNumberGenerator = new StubVerificationNumberHolder(CORRECT_CODE);
            long expectedExpiration = 60 * 3L;
            // when
            VerificationCode verificationStatus = VerificationCode.of(TEST_PHONE_NUMBER, verificationNumberGenerator, SMS);
            // then
            assertThat(verificationStatus.getId()).isEqualTo(TEST_PHONE_NUMBER);
            assertThat(verificationStatus.getVerificationCode()).isEqualTo(CORRECT_CODE);
            assertThat(verificationStatus.isVerified()).isFalse();
            assertThat(verificationStatus.getExpiration()).isEqualTo(expectedExpiration);
        }

        @Test
        void Email_인증_코드를_생성한다() {
            // given
            VerificationNumberGenerator verificationNumberGenerator = new StubVerificationNumberHolder(CORRECT_CODE);
            long expectedExpiration = 60 * 5L;
            // when
            VerificationCode verificationStatus = VerificationCode.of(TEST_EMAIL, verificationNumberGenerator, EMAIL);
            // then
            assertThat(verificationStatus.getId()).isEqualTo(TEST_EMAIL);
            assertThat(verificationStatus.getVerificationCode()).isEqualTo(CORRECT_CODE);
            assertThat(verificationStatus.isVerified()).isFalse();
            assertThat(verificationStatus.getExpiration()).isEqualTo(expectedExpiration);
        }
    }

    @Nested
    class detectAbnormalUsage {

        @Test
        void 최대_시도_미만이면_예외를_던지지_않는다() {
            // given
            Stream.generate(() -> WRONG_CODE)
                .limit(ABNORMAL_USAGE_THRESHOLD - 1)
                .forEach(code -> {
                    try { SMS_인증_코드.verify(code); }
                    catch (CustomException ignored) { }
                });
            // when / then
            assertDoesNotThrow(SMS_인증_코드::detectAbnormalUsage);
        }

        @Test
        void 최대_시도_도달하면_예외를_던진다() {
            // given
            Stream.generate(() -> WRONG_CODE)
                .limit(ABNORMAL_USAGE_THRESHOLD)
                .forEach(code -> {
                    try { SMS_인증_코드.verify(code); }
                    catch (CustomException ignored) { }
                });
            // when / then
            CustomException ex = assertThrows(
                CustomException.class,
                SMS_인증_코드::detectAbnormalUsage
            );
            assertEquals(ErrorCode.NOT_MATCHED_VERIFICATION_CODE, ex.getErrorCode());
        }
    }

    @Nested
    class verify {

        @Test
        void SMS_인증_코드를_검증한다() {
            // given
            long expectedExpiration = 60 * 60L;
            // when
            SMS_인증_코드.verify(CORRECT_CODE);
            // then
            assertThat(SMS_인증_코드.isVerified()).isTrue();
            assertThat(SMS_인증_코드.getExpiration()).isEqualTo(expectedExpiration);
        }

        @Test
        void Email_인증_코드를_검증한다() {
            // given
            long expectedExpiration = 60 * 60L;
            // when
            Email_인증_코드.verify(CORRECT_CODE);
            // then
            assertThat(Email_인증_코드.isVerified()).isTrue();
            assertThat(Email_인증_코드.getExpiration()).isEqualTo(expectedExpiration);
        }

        @Test
        void SMS_인증_코드가_다르면_예외를_던진다() {
            // when / then
            CustomException exception = assertThrows(CustomException.class, () -> SMS_인증_코드.verify(WRONG_CODE));
            assertEquals(ErrorCode.NOT_MATCHED_VERIFICATION_CODE, exception.getErrorCode());
        }

        @Test
        void Email_인증_코드가_다르면_예외를_던진다() {
            // when / then
            CustomException exception = assertThrows(CustomException.class, () -> Email_인증_코드.verify(WRONG_CODE));
            assertEquals(ErrorCode.NOT_MATCHED_VERIFICATION_CODE, exception.getErrorCode());
        }
    }

    @Nested
    class requireVerified {

        @Test
        void SMS_인증하면_예외를_던지지_않는다() {
            // given
            SMS_인증_코드.verify(CORRECT_CODE);
            // when / then
            assertDoesNotThrow(SMS_인증_코드::requireVerified);
        }

        @Test
        void SMS_미인증하면_예외를_던진다() {
            // when / then
            CustomException exception = assertThrows(CustomException.class, SMS_인증_코드::requireVerified);
            assertEquals(ErrorCode.FORBIDDEN_API, exception.getErrorCode());
        }

        @Test
        void Email_인증하면_예외를_던지지_않는다() {
            // given
            Email_인증_코드.verify(CORRECT_CODE);
            // when / then
            assertDoesNotThrow(Email_인증_코드::requireVerified);
        }

        @Test
        void Email_미인증하면_예외를_던진다() {
            // when / then
            CustomException exception = assertThrows(CustomException.class, Email_인증_코드::requireVerified);
            assertEquals(ErrorCode.FORBIDDEN_API, exception.getErrorCode());
        }
    }
}
