package in.koreatech.koin.unit.domain.user.verification.service;

import static in.koreatech.koin.domain.user.verification.model.VerificationChannel.EMAIL;
import static in.koreatech.koin.domain.user.verification.model.VerificationChannel.SMS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin._common.exception.errorcode.ErrorCode;
import in.koreatech.koin.domain.user.verification.dto.SendVerificationResponse;
import in.koreatech.koin.domain.user.verification.service.UserVerificationService;
import in.koreatech.koin.unit.domain.user.verification.mock.DummyApplicationEventPublisher;
import in.koreatech.koin.unit.domain.user.verification.mock.FakeVerificationCodeRedisRepository;
import in.koreatech.koin.unit.domain.user.verification.mock.FakeVerificationCountRedisRepository;
import in.koreatech.koin.unit.domain.user.verification.mock.StubVerificationNumberHolder;

@ExtendWith(MockitoExtension.class)
class UserVerificationServiceTest {

    private UserVerificationService userVerificationService;

    private static final int MAX_VERIFICATION_COUNT = 5;
    private static final String TEST_IP = "127.0.0.1";
    private static final String TEST_EMAIL = "user@koreatech.ac.kr";
    private static final String TEST_PHONE_NUMBER = "01012345678";
    private static final String CORRECT_CODE = "123456";
    private static final String WRONG_CODE = "999999";

    @BeforeEach
    void init() {
        FakeVerificationCodeRedisRepository fakeStatusRepository = new FakeVerificationCodeRedisRepository();
        FakeVerificationCountRedisRepository fakeCountRepository = new FakeVerificationCountRedisRepository();
        DummyApplicationEventPublisher fakeEventPublisher = new DummyApplicationEventPublisher();
        StubVerificationNumberHolder fakeGenerator = new StubVerificationNumberHolder(CORRECT_CODE);
        userVerificationService = new UserVerificationService(
            fakeStatusRepository,
            fakeCountRepository,
            fakeGenerator,
            fakeEventPublisher,
            MAX_VERIFICATION_COUNT
        );
    }

    @Nested
    class sendSmsVerification {

        @Test
        void 인증_횟수가_1로_초기화되고_인증번호가_발송된다() {
            // when
            SendVerificationResponse response = userVerificationService.sendVerification(TEST_PHONE_NUMBER, TEST_IP, SMS);
            // then
            assertThat(response.currentCount()).isEqualTo(1);
        }

        @Test
        void 인증_횟수가_증가하고_인증번호가_재발송된다() {
            // given
            userVerificationService.sendVerification(TEST_PHONE_NUMBER, TEST_IP, EMAIL);
            // when
            SendVerificationResponse response = userVerificationService.sendVerification(TEST_PHONE_NUMBER, TEST_IP, SMS);
            // then
            assertThat(response.currentCount()).isEqualTo(2);
        }

        @Test
        void 인증_횟수_초과_시_예외를_던진다() {
            // given
            Stream.generate(() -> userVerificationService)
                .limit(MAX_VERIFICATION_COUNT)
                .forEach((userVerification) -> {
                    userVerificationService.sendVerification(TEST_PHONE_NUMBER, TEST_IP, SMS);
                });
            // when / then
            CustomException exception = assertThrows(CustomException.class,
                () -> userVerificationService.sendVerification(TEST_PHONE_NUMBER, TEST_IP, SMS));
            assertEquals(ErrorCode.TOO_MANY_REQUESTS_VERIFICATION, exception.getErrorCode());
        }
    }

    @Nested
    class sendEmailVerification {

        @Test
        void 인증_횟수가_1로_초기화되고_인증번호가_발송된다() {
            // when
            SendVerificationResponse response = userVerificationService.sendVerification(TEST_EMAIL, TEST_IP, EMAIL);
            // then
            assertThat(response.currentCount()).isEqualTo(1);
        }

        @Test
        void 인증_횟수가_증가하고_인증번호가_재발송된다() {
            // given
            userVerificationService.sendVerification(TEST_EMAIL, TEST_IP, EMAIL);
            // when
            SendVerificationResponse response = userVerificationService.sendVerification(TEST_EMAIL, TEST_IP, EMAIL);
            // then
            assertThat(response.currentCount()).isEqualTo(2);
        }

        @Test
        void 인증_횟수_초과_시_예외를_던진다() {
            // given
            Stream.generate(() -> userVerificationService)
                .limit(MAX_VERIFICATION_COUNT)
                .forEach((userVerification) -> {
                    userVerificationService.sendVerification(TEST_EMAIL, TEST_IP, EMAIL);
                });
            // when / then
            CustomException exception = assertThrows(CustomException.class,
                () -> userVerificationService.sendVerification(TEST_EMAIL, TEST_IP, EMAIL));
            assertEquals(ErrorCode.TOO_MANY_REQUESTS_VERIFICATION, exception.getErrorCode());
        }
    }

    @Nested
    class verifyCode {

        @Test
        void SMS_인증번호가_일치하면_예외를_던지지_않는다() {
            // given
            userVerificationService.sendVerification(TEST_PHONE_NUMBER, TEST_IP, SMS);
            // when / then
            assertDoesNotThrow(() -> userVerificationService.verifyCode(TEST_PHONE_NUMBER, TEST_IP, CORRECT_CODE));
        }

        @Test
        void SMS_두번이상_일치해도_예외를_던지지_않는다() {
            // given
            userVerificationService.sendVerification(TEST_PHONE_NUMBER, TEST_IP, SMS);
            userVerificationService.verifyCode(TEST_PHONE_NUMBER, TEST_IP, CORRECT_CODE);
            // when / then
            assertDoesNotThrow(() -> userVerificationService.verifyCode(TEST_PHONE_NUMBER, TEST_IP, CORRECT_CODE));
        }

        @Test
        void SMS_인증번호가_불일치하면_예외를_던진다() {
            // given
            userVerificationService.sendVerification(TEST_PHONE_NUMBER, TEST_IP, SMS);
            // when / then
            CustomException exception = assertThrows(CustomException.class,
                () -> userVerificationService.verifyCode(TEST_PHONE_NUMBER, TEST_IP, WRONG_CODE));
            assertEquals(ErrorCode.NOT_MATCHED_VERIFICATION_CODE, exception.getErrorCode());
        }

        @Test
        void Email_인증번호가_일치하면_예외를_던지지_않는다() {
            // given
            userVerificationService.sendVerification(TEST_EMAIL, TEST_IP, EMAIL);
            // when / then
            assertDoesNotThrow(() -> userVerificationService.verifyCode(TEST_EMAIL, TEST_IP, CORRECT_CODE));
        }

        @Test
        void Email_두번이상_일치해도_예외를_던지지_않는다() {
            // given
            userVerificationService.sendVerification(TEST_EMAIL, TEST_IP, EMAIL);
            userVerificationService.verifyCode(TEST_EMAIL, TEST_IP, CORRECT_CODE);
            // when / then
            assertDoesNotThrow(() -> userVerificationService.verifyCode(TEST_EMAIL, TEST_IP, CORRECT_CODE));
        }

        @Test
        void Email_인증번호가_불일치하면_예외를_던진다() {
            // given
            userVerificationService.sendVerification(TEST_EMAIL, TEST_IP, EMAIL);
            // when / then
            CustomException exception = assertThrows(CustomException.class,
                () -> userVerificationService.verifyCode(TEST_EMAIL, TEST_IP, WRONG_CODE));
            assertEquals(ErrorCode.NOT_MATCHED_VERIFICATION_CODE, exception.getErrorCode());
        }
    }

    @Nested
    class detectAbnormalUsage {

        private static final int ABNORMAL_USAGE_THRESHOLD = 100;

        @Test
        void SMS_100회_이상_오입력하면_비정상_이용_예외를_던진다() {
            // given
            userVerificationService.sendVerification(TEST_PHONE_NUMBER, TEST_IP, SMS);
            Stream.generate(() -> WRONG_CODE)
                .limit(ABNORMAL_USAGE_THRESHOLD)
                .forEach(code -> {
                    try { userVerificationService.verifyCode(TEST_PHONE_NUMBER, TEST_IP, code); }
                    catch (CustomException ignored) { }
                });
            // when / then
            CustomException ex = assertThrows(
                CustomException.class,
                () -> userVerificationService.verifyCode(TEST_PHONE_NUMBER, TEST_IP, WRONG_CODE)
            );
            assertEquals(ErrorCode.NOT_MATCHED_VERIFICATION_CODE, ex.getErrorCode());
        }

        @Test
        void Email_100회_이상_오입력하면_비정상_이용_예외를_던진다() {
            // given
            userVerificationService.sendVerification(TEST_EMAIL, TEST_IP, EMAIL);
            Stream.generate(() -> WRONG_CODE)
                .limit(ABNORMAL_USAGE_THRESHOLD)
                .forEach(code -> {
                    try { userVerificationService.verifyCode(TEST_EMAIL, TEST_IP, code); }
                    catch (CustomException ignored) { }
                });
            // when / then
            CustomException ex = assertThrows(
                CustomException.class,
                () -> userVerificationService.verifyCode(TEST_EMAIL, TEST_IP, WRONG_CODE)
            );
            assertEquals(ErrorCode.NOT_MATCHED_VERIFICATION_CODE, ex.getErrorCode());
        }
    }

    @Nested
    class consumeVerification {

        @Test
        void SMS_인증_정보를_삭제한다() {
            // given
            userVerificationService.sendVerification(TEST_PHONE_NUMBER, TEST_IP, SMS);
            userVerificationService.verifyCode(TEST_PHONE_NUMBER, TEST_IP, CORRECT_CODE);
            // when / then
            assertDoesNotThrow(() -> userVerificationService.consumeVerification(TEST_PHONE_NUMBER));
        }

        @Test
        void SMS_미인증_상태이면_예외를_던진다() {
            // when / then
            CustomException exception = assertThrows(CustomException.class,
                () -> userVerificationService.consumeVerification(TEST_PHONE_NUMBER));
            assertEquals(ErrorCode.FORBIDDEN_API, exception.getErrorCode());
        }

        @Test
        void SMS_미검증_상태이면_예외를_던진다() {
            // given
            userVerificationService.sendVerification(TEST_PHONE_NUMBER, TEST_IP, SMS);
            // when / then
            CustomException exception = assertThrows(CustomException.class,
                () -> userVerificationService.consumeVerification(TEST_PHONE_NUMBER));
            assertEquals(ErrorCode.FORBIDDEN_API, exception.getErrorCode());
        }

        @Test
        void Email_인증_정보를_삭제한다() {
            // given
            userVerificationService.sendVerification(TEST_EMAIL, TEST_IP, EMAIL);
            userVerificationService.verifyCode(TEST_EMAIL, TEST_IP, CORRECT_CODE);
            // when / then
            assertDoesNotThrow(() -> userVerificationService.consumeVerification(TEST_EMAIL));
        }

        @Test
        void Email_미인증_상태이면_예외를_던진다() {
            // when / then
            CustomException exception = assertThrows(CustomException.class,
                () -> userVerificationService.consumeVerification(TEST_EMAIL));
            assertEquals(ErrorCode.FORBIDDEN_API, exception.getErrorCode());
        }

        @Test
        void Email_미검증_상태이면_예외를_던진다() {
            // given
            userVerificationService.sendVerification(TEST_EMAIL, TEST_IP, EMAIL);
            // when / then
            CustomException exception = assertThrows(CustomException.class,
                () -> userVerificationService.consumeVerification(TEST_EMAIL));
            assertEquals(ErrorCode.FORBIDDEN_API, exception.getErrorCode());
        }
    }
}
