package in.koreatech.koin.unit.domain.user.verification.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
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
import in.koreatech.koin.unit.domain.user.verification.mock.TestVerificationNumberHolder;

@ExtendWith(MockitoExtension.class)
class UserVerificationServiceTest {

    private UserVerificationService userVerificationService;

    private static final String TEST_EMAIL = "user@koreatech.ac.kr";
    private static final String TEST_PHONE_NUMBER = "01012345678";
    private static final String CORRECT_CODE = "123456";
    private static final String WRONG_CODE = "999999";

    @BeforeEach
    void init() {
        FakeUserVerificationStatusRedisRepository fakeStatusRepository = new FakeUserVerificationStatusRedisRepository();
        FakeUserDailyVerificationCountRedisRepository fakeCountRepository = new FakeUserDailyVerificationCountRedisRepository();
        FakeApplicationEventPublisher fakeEventPublisher = new FakeApplicationEventPublisher();
        TestVerificationNumberHolder fakeGenerator = new TestVerificationNumberHolder(CORRECT_CODE);
        userVerificationService = new UserVerificationService(
            fakeStatusRepository,
            fakeCountRepository,
            fakeGenerator,
            fakeEventPublisher
        );
    }

    @Nested
    class sendSmsVerification {

        @Test
        void sendSmsVerification를_최초_호출하면_인증_횟수가_1로_초기화되고_인증번호가_발송된다() {
            // when
            SendVerificationResponse response = userVerificationService.sendSmsVerification(TEST_PHONE_NUMBER);

            // then
            assertThat(response.currentCount()).isEqualTo(1);
        }

        @Test
        void sendSmsVerification를_다시_호출하면_인증_횟수가_증가하고_인증번호가_재발송된다() {
            // given
            userVerificationService.sendSmsVerification(TEST_PHONE_NUMBER);

            // when
            SendVerificationResponse response = userVerificationService.sendSmsVerification(TEST_PHONE_NUMBER);

            // then
            assertThat(response.currentCount()).isEqualTo(2);
        }

        @Test
        void 일일_인증_횟수_초과_시_sendSmsVerification를_호출하면_TooManyRequestException이_발생한다() {
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
    class sendEmailVerification {

        @Test
        void sendEmailVerification를_최초_호출하면_인증_횟수가_1로_초기화되고_인증번호가_발송된다() {
            // when
            SendVerificationResponse response = userVerificationService.sendEmailVerification(TEST_EMAIL);

            // then
            assertThat(response.currentCount()).isEqualTo(1);
        }

        @Test
        void sendEmailVerification를_다시_호출하면_인증_횟수가_증가하고_인증번호가_재발송된다() {
            // given
            userVerificationService.sendEmailVerification(TEST_EMAIL);

            // when
            SendVerificationResponse response = userVerificationService.sendEmailVerification(TEST_EMAIL);

            // then
            assertThat(response.currentCount()).isEqualTo(2);
        }

        @Test
        void 일일_인증_횟수_초과_시_sendEmailVerification를_호출하면_TooManyRequestException이_발생한다() {
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
    class verifyCode {

        @Test
        void 올바른_SMS_인증번호를_입력하여_verifyCode를_호출하면_예외를_반환하지_않는다() {
            // given
            userVerificationService.sendEmailVerification(TEST_PHONE_NUMBER);

            // when / then
            assertThatCode(() -> userVerificationService.verifyCode(TEST_PHONE_NUMBER, CORRECT_CODE))
                .doesNotThrowAnyException();
        }

        @Test
        void 올바른_SMS_인증번호를_입력하여_verifyCode를_재호출해도_예외를_반환하지_않는다() {
            // given
            userVerificationService.sendEmailVerification(TEST_PHONE_NUMBER);
            userVerificationService.verifyCode(TEST_PHONE_NUMBER, CORRECT_CODE);

            // when / then
            assertThatCode(() -> userVerificationService.verifyCode(TEST_PHONE_NUMBER, CORRECT_CODE))
                .doesNotThrowAnyException();
        }

        @Test
        void 잘못된_SMS_인증번호를_입력하여_verifyCode를_호출하면_KoinIllegalArgumentException이_발생한다() {
            // given
            userVerificationService.sendEmailVerification(TEST_PHONE_NUMBER);

            // when / then
            assertThatThrownBy(() -> userVerificationService.verifyCode(TEST_PHONE_NUMBER, WRONG_CODE))
                .isInstanceOf(KoinIllegalArgumentException.class);
        }

        @Test
        void 올바른_이메일_인증번호를_입력하여_verifyCode를_호출하면_예외를_반환하지_않는다() {
            // given
            userVerificationService.sendEmailVerification(TEST_EMAIL);

            // when / then
            assertThatCode(() -> userVerificationService.verifyCode(TEST_EMAIL, CORRECT_CODE))
                .doesNotThrowAnyException();
        }

        @Test
        void 올바른_이메일_인증번호를_입력하여_verifyCode를_재호출해도_예외를_반환하지_않는다() {
            // given
            userVerificationService.sendEmailVerification(TEST_EMAIL);
            userVerificationService.verifyCode(TEST_EMAIL, CORRECT_CODE);

            // when / then
            assertThatCode(() -> userVerificationService.verifyCode(TEST_EMAIL, CORRECT_CODE))
                .doesNotThrowAnyException();
        }

        @Test
        void 잘못된_이메일_인증번호를_입력하여_verifyCode를_호출하면_KoinIllegalArgumentException이_발생한다() {
            // given
            userVerificationService.sendEmailVerification(TEST_EMAIL);

            // when / then
            assertThatThrownBy(() -> userVerificationService.verifyCode(TEST_EMAIL, WRONG_CODE))
                .isInstanceOf(KoinIllegalArgumentException.class);
        }
    }

    @Nested
    class consumeVerification {

        @Test
        void SMS_인증_완료된_상태에서_요청하면_인증_정보가_삭제된다() {
            // given
            userVerificationService.sendEmailVerification(TEST_PHONE_NUMBER);
            userVerificationService.verifyCode(TEST_PHONE_NUMBER, CORRECT_CODE);

            // when / then
            assertThatCode(() -> userVerificationService.consumeVerification(TEST_PHONE_NUMBER))
                .doesNotThrowAnyException();
        }

        @Test
        void SMS_미인증_상태에서_요청하면_AuthorizationException이_발생한다() {
            // when / then
            assertThatThrownBy(() -> userVerificationService.consumeVerification(TEST_PHONE_NUMBER))
                .isInstanceOf(AuthorizationException.class);
        }

        @Test
        void SMS_미검증_상태에서_요청하면_AuthorizationException이_발생한다() {
            // given
            userVerificationService.sendEmailVerification(TEST_PHONE_NUMBER);

            // when / then
            assertThatThrownBy(() -> userVerificationService.consumeVerification(TEST_PHONE_NUMBER))
                .isInstanceOf(AuthorizationException.class);
        }

        @Test
        void 이메일_인증_완료된_상태에서_요청하면_인증_정보가_삭제된다() {
            // given
            userVerificationService.sendEmailVerification(TEST_EMAIL);
            userVerificationService.verifyCode(TEST_EMAIL, CORRECT_CODE);

            // when / then
            assertThatCode(() -> userVerificationService.consumeVerification(TEST_EMAIL))
                .doesNotThrowAnyException();
        }

        @Test
        void 이메일_미인증_상태에서_요청하면_AuthorizationException이_발생한다() {
            // when / then
            assertThatThrownBy(() -> userVerificationService.consumeVerification(TEST_EMAIL))
                .isInstanceOf(AuthorizationException.class);
        }

        @Test
        void 이메일_미검증_상태에서_요청하면_AuthorizationException이_발생한다() {
            // given
            userVerificationService.sendEmailVerification(TEST_EMAIL);

            // when / then
            assertThatThrownBy(() -> userVerificationService.consumeVerification(TEST_EMAIL))
                .isInstanceOf(AuthorizationException.class);
        }
    }
}
