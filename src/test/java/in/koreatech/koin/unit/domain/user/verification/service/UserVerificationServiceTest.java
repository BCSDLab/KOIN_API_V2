package in.koreatech.koin.unit.domain.user.verification.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin._common.exception.custom.TooManyRequestsException;
import in.koreatech.koin.domain.user.verification.dto.SendVerificationResponse;
import in.koreatech.koin.domain.user.verification.service.UserVerificationService;
import in.koreatech.koin.unit.domain.user.verification.mock.FakeApplicationEventPublisher;
import in.koreatech.koin.unit.domain.user.verification.mock.FakeUserDailyVerificationCountRedisRepository;
import in.koreatech.koin.unit.domain.user.verification.mock.FakeUserVerificationStatusRedisRepository;
import in.koreatech.koin.unit.domain.user.verification.mock.FakeVerificationNumberGenerator;

@ExtendWith(MockitoExtension.class)
class UserVerificationServiceTest {

    private UserVerificationService userVerificationService;

    private static final String TEST_EMAIL = "test@koreatech.ac.kr";
    private static final String TEST_PHONE_NUMBER = "01012345678";
    private static final String CORRECT_CODE = "123456";
    private static final String WRONG_CODE = "999999";

    @BeforeEach
    void init() {
        FakeUserVerificationStatusRedisRepository fakeStatusRepository = new FakeUserVerificationStatusRedisRepository();
        FakeUserDailyVerificationCountRedisRepository fakeCountRepository = new FakeUserDailyVerificationCountRedisRepository();
        FakeApplicationEventPublisher fakeEventPublisher = new FakeApplicationEventPublisher();
        FakeVerificationNumberGenerator fakeGenerator = new FakeVerificationNumberGenerator(CORRECT_CODE);
        userVerificationService = new UserVerificationService(
            fakeStatusRepository,
            fakeCountRepository,
            fakeGenerator,
            fakeEventPublisher
        );
    }

    @Nested
    @DisplayName("Sms 인증번호 발송 테스트")
    class SendSmsVerificationTest {

        @Test
        @DisplayName("최초 인증 요청 시, 일일 인증 횟수가 1로 초기화되고 인증번호가 발송된다")
        void sendFirstTime() {
            // when
            SendVerificationResponse response = userVerificationService.sendSmsVerification(TEST_PHONE_NUMBER);

            // then
            assertThat(response.currentCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("기존 인증 내역이 있을 시, 일일 인증 횟수가 1 증가하고 인증번호가 발송된다")
        void sendAgain() {
            // given
            userVerificationService.sendSmsVerification(TEST_PHONE_NUMBER);

            // when
            SendVerificationResponse response = userVerificationService.sendSmsVerification(TEST_PHONE_NUMBER);

            // then
            assertThat(response.currentCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("일일 인증 횟수 초과 시, TooManyRequestException이 발생한다")
        void sendFail() {
            // given
            userVerificationService.sendSmsVerification(TEST_PHONE_NUMBER);
            userVerificationService.sendSmsVerification(TEST_PHONE_NUMBER);
            userVerificationService.sendSmsVerification(TEST_PHONE_NUMBER);
            userVerificationService.sendSmsVerification(TEST_PHONE_NUMBER);
            userVerificationService.sendSmsVerification(TEST_PHONE_NUMBER);

            // when / then
            assertThatThrownBy(() -> userVerificationService.sendSmsVerification(TEST_PHONE_NUMBER))
                .isInstanceOf(TooManyRequestsException.class);
        }
    }

    @Nested
    @DisplayName("Email 인증번호 발송 테스트")
    class SendEmailVerificationTest {

        @Test
        @DisplayName("최초 인증 요청 시, 일일 인증 횟수가 1로 초기화되고 인증번호가 발송된다")
        void sendFirstTime() {
            // when
            SendVerificationResponse response = userVerificationService.sendEmailVerification(TEST_EMAIL);

            // then
            assertThat(response.currentCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("기존 인증 내역이 있을 시, 일일 인증 횟수가 1 증가하고 인증번호가 발송된다")
        void sendAgain() {
            // given
            userVerificationService.sendEmailVerification(TEST_EMAIL);

            // when
            SendVerificationResponse response = userVerificationService.sendEmailVerification(TEST_EMAIL);

            // then
            assertThat(response.currentCount()).isEqualTo(2);
        }

        @Test
        @DisplayName("일일 인증 횟수 초과 시, TooManyRequestException이 발생한다")
        void sendFail() {
            // given
            userVerificationService.sendEmailVerification(TEST_EMAIL);
            userVerificationService.sendEmailVerification(TEST_EMAIL);
            userVerificationService.sendEmailVerification(TEST_EMAIL);
            userVerificationService.sendEmailVerification(TEST_EMAIL);
            userVerificationService.sendEmailVerification(TEST_EMAIL);

            // when / then
            assertThatThrownBy(() -> userVerificationService.sendEmailVerification(TEST_EMAIL))
                .isInstanceOf(TooManyRequestsException.class);
        }
    }

    @Nested
    @DisplayName("인증번호 검증 테스트")
    class VerifyCodeTest {

        @Test
        @DisplayName("올바른 인증번호를 입력하면, 예외를 반환하지 않는다")
        void verifySuccess() {
            // given
            userVerificationService.sendEmailVerification(TEST_EMAIL);

            // when / then
            assertThatCode(() -> userVerificationService.verifyCode(TEST_EMAIL, CORRECT_CODE))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("올바른 인증번호를 두번 이상 입력해도, 예외를 반환하지 않는다")
        void verifyAgain() {
            // given
            userVerificationService.sendEmailVerification(TEST_EMAIL);
            userVerificationService.verifyCode(TEST_EMAIL, CORRECT_CODE);

            // when / then
            assertThatCode(() -> userVerificationService.verifyCode(TEST_EMAIL, CORRECT_CODE))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("잘못된 인증번호를 입력하면 KoinIllegalArgumentException이 발생한다")
        void verifyFail() {
            // given
            userVerificationService.sendEmailVerification(TEST_EMAIL);

            // when / then
            assertThatThrownBy(() -> userVerificationService.verifyCode(TEST_EMAIL, WRONG_CODE))
                .isInstanceOf(KoinIllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("인증 정보 소모(사용) 테스트")
    class ConsumeVerificationTest {

        @Test
        @DisplayName("인증 완료된 상태에서 요청하면, 인증 정보가 삭제된다")
        void consumeSuccess() {
            // given
            userVerificationService.sendEmailVerification(TEST_EMAIL);
            userVerificationService.verifyCode(TEST_EMAIL, CORRECT_CODE);

            // when / then
            assertThatCode(() -> userVerificationService.consumeVerification(TEST_EMAIL))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("미인증 상태에서 요청하면, AuthorizationException이 발생한다")
        void consumeFail() {
            // given
            userVerificationService.sendEmailVerification(TEST_EMAIL);

            // when / then
            assertThatThrownBy(() -> userVerificationService.consumeVerification(TEST_EMAIL))
                .isInstanceOf(AuthorizationException.class);
        }

        @Test
        @DisplayName("인증 코드 미생성 상태에서 요청하면, AuthorizationException이 발생한다")
        void consumeFail2() {
            // when / then
            assertThatThrownBy(() -> userVerificationService.consumeVerification(TEST_EMAIL))
                .isInstanceOf(AuthorizationException.class);
        }
    }
}
