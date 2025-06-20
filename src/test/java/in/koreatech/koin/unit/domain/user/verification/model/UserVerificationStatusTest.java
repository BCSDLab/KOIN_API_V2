package in.koreatech.koin.unit.domain.user.verification.model;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin._common.util.random.VerificationNumberGenerator;
import in.koreatech.koin.domain.user.verification.model.UserVerificationStatus;
import in.koreatech.koin.unit.domain.user.verification.mock.StubVerificationNumberHolder;
import in.koreatech.koin.unit.fixutre.VerificationFixture;

class UserVerificationStatusTest {

    private UserVerificationStatus SMS_인증_상태;
    private UserVerificationStatus Email_인증_상태;

    private static final String TEST_EMAIL = "user@koreatech.ac.kr";
    private static final String TEST_PHONE_NUMBER = "01012345678";
    private static final String CORRECT_CODE = "123456";
    private static final String WRONG_CODE = "999999";

    @BeforeEach
    void init() {
        SMS_인증_상태 = VerificationFixture.SMS_인증_상태(TEST_PHONE_NUMBER, CORRECT_CODE);
        Email_인증_상태 = VerificationFixture.Email_인증_상태(TEST_EMAIL, CORRECT_CODE);
    }

    @Nested
    class create {

        @Test
        void createBySms를_이용하여_SMS_인증용_객체를_생성할_수_있다() {
            // given
            VerificationNumberGenerator verificationNumberGenerator = new StubVerificationNumberHolder(CORRECT_CODE);
            long expectedExpiration = 60 * 3L;

            // when
            UserVerificationStatus verificationStatus = UserVerificationStatus.ofSms(TEST_PHONE_NUMBER, verificationNumberGenerator);

            // then
            assertThat(verificationStatus.getId()).isEqualTo(TEST_PHONE_NUMBER);
            assertThat(verificationStatus.getVerificationCode()).isEqualTo(CORRECT_CODE);
            assertThat(verificationStatus.isVerified()).isFalse();
            assertThat(verificationStatus.getExpiration()).isEqualTo(expectedExpiration);
        }

        @Test
        void createByEmail를_이용하여_Email_인증용_객체를_생성할_수_있다() {
            // given
            VerificationNumberGenerator verificationNumberGenerator = new StubVerificationNumberHolder(CORRECT_CODE);
            long expectedExpiration = 60 * 5L;

            // when
            UserVerificationStatus verificationStatus = UserVerificationStatus.ofEmail(TEST_EMAIL, verificationNumberGenerator);

            // then
            assertThat(verificationStatus.getId()).isEqualTo(TEST_EMAIL);
            assertThat(verificationStatus.getVerificationCode()).isEqualTo(CORRECT_CODE);
            assertThat(verificationStatus.isVerified()).isFalse();
            assertThat(verificationStatus.getExpiration()).isEqualTo(expectedExpiration);
        }
    }

    @Nested
    class verify {

        @Test
        void verify를_이용하여_SMS_인증용_객체를_인증할_수_있다() {
            // given
            long expectedExpiration = 60 * 60L;

            // when
            SMS_인증_상태.verify(CORRECT_CODE);

            // then
            assertThat(SMS_인증_상태.isVerified()).isTrue();
            assertThat(SMS_인증_상태.getExpiration()).isEqualTo(expectedExpiration);
        }

        @Test
        void verify를_이용하여_Email_인증용_객체를_인증할_수_있다() {
            // given
            long expectedExpiration = 60 * 60L;

            // when
            Email_인증_상태.verify(CORRECT_CODE);

            // then
            assertThat(Email_인증_상태.isVerified()).isTrue();
            assertThat(Email_인증_상태.getExpiration()).isEqualTo(expectedExpiration);
        }

        @Test
        void SMS_인증용_객체에_잘못된_인증번호를_입력하면_KoinIllegalArgumentException이_발생한다() {
            // when / then
            assertThatThrownBy(() -> SMS_인증_상태.verify(WRONG_CODE))
                .isInstanceOf(KoinIllegalArgumentException.class)
                .hasMessage("인증 번호가 일치하지 않습니다.");
        }

        @Test
        void Email_인증용_객체에_잘못된_인증번호를_입력하면_KoinIllegalArgumentException이_발생한다() {
            // when / then
            assertThatThrownBy(() -> Email_인증_상태.verify(WRONG_CODE))
                .isInstanceOf(KoinIllegalArgumentException.class)
                .hasMessage("인증 번호가 일치하지 않습니다.");
        }
    }

    @Nested
    class requireVerified {

        @Test
        void SMS_인증된_상태에서_requireVerified를_호출하면_예외가_발생하지_않는다() {
            // given
            SMS_인증_상태.verify(CORRECT_CODE);

            // when / then
            assertThatCode(SMS_인증_상태::requireVerified)
                .doesNotThrowAnyException();
        }

        @Test
        void SMS_미인증_상태에서_requireVerified를_호출하면_AuthorizationException이_발생한다() {
            // when / then
            assertThatThrownBy(SMS_인증_상태::requireVerified)
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("본인 인증 후 다시 시도해주십시오.");
        }

        @Test
        void Email_인증된_상태에서_requireVerified를_호출하면_예외가_발생하지_않는다() {
            // given
            Email_인증_상태.verify(CORRECT_CODE);

            // when / then
            assertThatCode(Email_인증_상태::requireVerified)
                .doesNotThrowAnyException();
        }

        @Test
        void Email_미인증_상태에서_requireVerified를_호출하면_AuthorizationException이_발생한다() {
            // when / then
            assertThatThrownBy(Email_인증_상태::requireVerified)
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("본인 인증 후 다시 시도해주십시오.");
        }
    }
}
