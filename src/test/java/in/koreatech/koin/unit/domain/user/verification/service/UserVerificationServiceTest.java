package in.koreatech.koin.unit.domain.user.verification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin._common.code.ApiResponseCode;
import in.koreatech.koin.domain.user.verification.config.VerificationProperties;
import in.koreatech.koin.domain.user.verification.dto.SendVerificationResponse;
import in.koreatech.koin.domain.user.verification.service.UserVerificationService;
import in.koreatech.koin.unit.domain.user.verification.mock.DummyApplicationEventPublisher;
import in.koreatech.koin.unit.domain.user.verification.mock.FakeUserDailyVerificationCountRedisRepository;
import in.koreatech.koin.unit.domain.user.verification.mock.FakeUserVerificationStatusRedisRepository;
import in.koreatech.koin.unit.domain.user.verification.mock.StubVerificationNumberHolder;

@ExtendWith(MockitoExtension.class)
class UserVerificationServiceTest {

    private UserVerificationService userVerificationService;

    private static final String TEST_EMAIL = "user@koreatech.ac.kr";
    private static final String TEST_PHONE_NUMBER = "01012345678";
    private static final String CORRECT_CODE = "123456";
    private static final String WRONG_CODE = "999999";
    private static final int MAX_VERIFICATION_COUNT = 5;

    @BeforeEach
    void init() {
        FakeUserVerificationStatusRedisRepository fakeStatusRepository = new FakeUserVerificationStatusRedisRepository();
        FakeUserDailyVerificationCountRedisRepository fakeCountRepository = new FakeUserDailyVerificationCountRedisRepository();
        DummyApplicationEventPublisher fakeEventPublisher = new DummyApplicationEventPublisher();
        StubVerificationNumberHolder fakeGenerator = new StubVerificationNumberHolder(CORRECT_CODE);
        VerificationProperties verificationProperties = new VerificationProperties(MAX_VERIFICATION_COUNT);
        userVerificationService = new UserVerificationService(
            fakeStatusRepository,
            fakeCountRepository,
            fakeGenerator,
            fakeEventPublisher,
            verificationProperties
        );
    }

    @Nested
    class sendSmsVerification {

        @Test
        void 인증_횟수가_1로_초기화되고_인증번호가_발송된다() {
            // when
            SendVerificationResponse response = userVerificationService.sendSmsVerification(TEST_PHONE_NUMBER);
            // then
            assertThat(response.currentCount()).isEqualTo(1);
        }

        @Test
        void 인증_횟수가_증가하고_인증번호가_재발송된다() {
            // given
            userVerificationService.sendSmsVerification(TEST_PHONE_NUMBER);
            // when
            SendVerificationResponse response = userVerificationService.sendSmsVerification(TEST_PHONE_NUMBER);
            // then
            assertThat(response.currentCount()).isEqualTo(2);
        }

        @Test
        void 인증_횟수_초과_시_예외를_던진다() {
            // given
            Stream.generate(() -> userVerificationService)
                .limit(MAX_VERIFICATION_COUNT)
                .forEach((userVerification) -> {
                    userVerificationService.sendSmsVerification(TEST_PHONE_NUMBER);
                });
            // when / then
            CustomException exception = assertThrows(CustomException.class, () -> userVerificationService.sendEmailVerification(TEST_PHONE_NUMBER));
            assertEquals(ApiResponseCode.TOO_MANY_REQUESTS_VERIFICATION, exception.getUserErrorCode());
        }
    }

    @Nested
    class sendEmailVerification {

        @Test
        void 인증_횟수가_1로_초기화되고_인증번호가_발송된다() {
            // when
            SendVerificationResponse response = userVerificationService.sendEmailVerification(TEST_EMAIL);
            // then
            assertThat(response.currentCount()).isEqualTo(1);
        }

        @Test
        void 인증_횟수가_증가하고_인증번호가_재발송된다() {
            // given
            userVerificationService.sendEmailVerification(TEST_EMAIL);
            // when
            SendVerificationResponse response = userVerificationService.sendEmailVerification(TEST_EMAIL);
            // then
            assertThat(response.currentCount()).isEqualTo(2);
        }

        @Test
        void 인증_횟수_초과_시_예외를_던진다() {
            // given
            userVerificationService.sendEmailVerification(TEST_EMAIL);
            userVerificationService.sendEmailVerification(TEST_EMAIL);
            userVerificationService.sendEmailVerification(TEST_EMAIL);
            userVerificationService.sendEmailVerification(TEST_EMAIL);
            userVerificationService.sendEmailVerification(TEST_EMAIL);
            // when / then
            CustomException exception = assertThrows(CustomException.class, () -> userVerificationService.sendEmailVerification(TEST_EMAIL));
            assertEquals(ApiResponseCode.TOO_MANY_REQUESTS_VERIFICATION, exception.getUserErrorCode());
        }
    }

    @Nested
    class verifyCode {

        @Test
        void SMS_인증번호가_일치하면_예외를_던지지_않는다() {
            // given
            userVerificationService.sendSmsVerification(TEST_PHONE_NUMBER);
            // when / then
            assertDoesNotThrow(() -> userVerificationService.verifyCode(TEST_PHONE_NUMBER, CORRECT_CODE));
        }

        @Test
        void SMS_두번이상_일치해도_예외를_던지지_않는다() {
            // given
            userVerificationService.sendSmsVerification(TEST_PHONE_NUMBER);
            userVerificationService.verifyCode(TEST_PHONE_NUMBER, CORRECT_CODE);
            // when / then
            assertDoesNotThrow(() -> userVerificationService.verifyCode(TEST_PHONE_NUMBER, CORRECT_CODE));
        }

        @Test
        void SMS_인증번호가_불일치하면_예외를_던진다() {
            // given
            userVerificationService.sendSmsVerification(TEST_PHONE_NUMBER);
            // when / then
            CustomException exception = assertThrows(CustomException.class, () -> userVerificationService.verifyCode(TEST_PHONE_NUMBER, WRONG_CODE));
            assertEquals(ApiResponseCode.NOT_MATCHED_VERIFICATION_CODE, exception.getUserErrorCode());
        }

        @Test
        void Email_인증번호가_일치하면_예외를_던지지_않는다() {
            // given
            userVerificationService.sendEmailVerification(TEST_EMAIL);
            // when / then
            assertDoesNotThrow(() -> userVerificationService.verifyCode(TEST_EMAIL, CORRECT_CODE));
        }

        @Test
        void Email_두번이상_일치해도_예외를_던지지_않는다() {
            // given
            userVerificationService.sendEmailVerification(TEST_EMAIL);
            userVerificationService.verifyCode(TEST_EMAIL, CORRECT_CODE);
            // when / then
            assertDoesNotThrow(() -> userVerificationService.verifyCode(TEST_EMAIL, CORRECT_CODE));
        }

        @Test
        void Email_인증번호가_불일치하면_예외를_던진다() {
            // given
            userVerificationService.sendEmailVerification(TEST_EMAIL);
            // when / then
            CustomException exception = assertThrows(CustomException.class, () -> userVerificationService.verifyCode(TEST_EMAIL, WRONG_CODE));
            assertEquals(ApiResponseCode.NOT_MATCHED_VERIFICATION_CODE, exception.getUserErrorCode());
        }
    }

    @Nested
    class consumeVerification {

        @Test
        void SMS_인증_정보를_삭제한다() {
            // given
            userVerificationService.sendSmsVerification(TEST_PHONE_NUMBER);
            userVerificationService.verifyCode(TEST_PHONE_NUMBER, CORRECT_CODE);
            // when / then
            assertDoesNotThrow(() -> userVerificationService.consumeVerification(TEST_PHONE_NUMBER));
        }

        @Test
        void SMS_미인증_상태이면_예외를_던진다() {
            // when / then
            CustomException exception = assertThrows(CustomException.class, () -> userVerificationService.consumeVerification(TEST_PHONE_NUMBER));
            assertEquals(ApiResponseCode.FORBIDDEN_VERIFICATION, exception.getUserErrorCode());
        }

        @Test
        void SMS_미검증_상태이면_예외를_던진다() {
            // given
            userVerificationService.sendSmsVerification(TEST_PHONE_NUMBER);
            // when / then
            CustomException exception = assertThrows(CustomException.class, () -> userVerificationService.consumeVerification(TEST_PHONE_NUMBER));
            assertEquals(ApiResponseCode.FORBIDDEN_VERIFICATION, exception.getUserErrorCode());
        }

        @Test
        void Email_인증_정보를_삭제한다() {
            // given
            userVerificationService.sendEmailVerification(TEST_EMAIL);
            userVerificationService.verifyCode(TEST_EMAIL, CORRECT_CODE);
            // when / then
            assertDoesNotThrow(() -> userVerificationService.consumeVerification(TEST_EMAIL));
        }

        @Test
        void Email_미인증_상태이면_예외를_던진다() {
            // when / then
            CustomException exception = assertThrows(CustomException.class, () -> userVerificationService.consumeVerification(TEST_EMAIL));
            assertEquals(ApiResponseCode.FORBIDDEN_VERIFICATION, exception.getUserErrorCode());
        }

        @Test
        void Email_미검증_상태이면_예외를_던진다() {
            // given
            userVerificationService.sendEmailVerification(TEST_EMAIL);
            // when / then
            CustomException exception = assertThrows(CustomException.class, () -> userVerificationService.consumeVerification(TEST_EMAIL));
            assertEquals(ApiResponseCode.FORBIDDEN_VERIFICATION, exception.getUserErrorCode());
        }
    }
}
