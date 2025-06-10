package in.koreatech.koin.unit.domain.user.verification.model;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import in.koreatech.koin._common.util.random.VerificationNumberGenerator;
import in.koreatech.koin.domain.user.verification.model.UserVerificationStatus;
import in.koreatech.koin.unit.domain.user.verification.mock.TestVerificationNumberHolder;
import in.koreatech.koin.unit.fixutre.UserVerificationStatusFixture;

class UserVerificationStatusTest {

    private UserVerificationStatus SMS_인증;
    private UserVerificationStatus 이메일_인증;

    private static final String TEST_EMAIL = "user@koreatech.ac.kr";
    private static final String TEST_PHONE_NUMBER = "01012345678";
    private static final String CORRECT_CODE = "123456";
    private static final String WRONG_CODE = "999999";

    @BeforeEach
    void init() {
        SMS_인증 = UserVerificationStatusFixture.SMS_인증(TEST_PHONE_NUMBER, CORRECT_CODE);
        이메일_인증 = UserVerificationStatusFixture.이메일_인증(TEST_EMAIL, CORRECT_CODE);
    }

    @Nested
    @DisplayName("인증 유효시간 테스트")
    class create {

        @Test
        void createBySms를_이용하여_SMS_인증용_객체를_생성할_수_있다() {
            // given
            VerificationNumberGenerator verificationNumberGenerator = new TestVerificationNumberHolder(CORRECT_CODE);
            long expectedExpiration = 60 * 3L;

            // when
            UserVerificationStatus verificationStatus = UserVerificationStatus.createBySms(TEST_PHONE_NUMBER, verificationNumberGenerator);

            // then
            assertThat(verificationStatus.getId()).isEqualTo(TEST_PHONE_NUMBER);
            assertThat(verificationStatus.getVerificationCode()).isEqualTo(CORRECT_CODE);
            assertThat(verificationStatus.isVerified()).isFalse();
            assertThat(verificationStatus.getExpiration()).isEqualTo(expectedExpiration);
        }

        @Test
        void createByEmail를_이용하여_이메일_인증용_객체를_생성할_수_있다() {
            // given
            VerificationNumberGenerator verificationNumberGenerator = new TestVerificationNumberHolder(CORRECT_CODE);
            long expectedExpiration = 60 * 5L;

            // when
            UserVerificationStatus verificationStatus = UserVerificationStatus.createByEmail(TEST_EMAIL, verificationNumberGenerator);

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
            SMS_인증.verify(CORRECT_CODE);

            // then
            assertThat(SMS_인증.isVerified()).isTrue();
            assertThat(SMS_인증.getExpiration()).isEqualTo(expectedExpiration);
        }

        @Test
        void verify를_이용하여_이메일_인증용_객체를_인증할_수_있다() {
            // given
            long expectedExpiration = 60 * 60L;

            // when
            이메일_인증.verify(CORRECT_CODE);

            // then
            assertThat(이메일_인증.isVerified()).isTrue();
            assertThat(이메일_인증.getExpiration()).isEqualTo(expectedExpiration);
        }

        @Test
        void SMS_인증용_객체에_잘못된_인증번호를_입력하면_KoinIllegalArgumentException이_발생한다() {
            // when / then
            assertThatThrownBy(() -> SMS_인증.verify(WRONG_CODE))
                .isInstanceOf(KoinIllegalArgumentException.class)
                .hasMessage("인증 번호가 일치하지 않습니다.");
        }

        @Test
        void 이메일_인증용_객체에_잘못된_인증번호를_입력하면_KoinIllegalArgumentException이_발생한다() {
            // when / then
            assertThatThrownBy(() -> 이메일_인증.verify(WRONG_CODE))
                .isInstanceOf(KoinIllegalArgumentException.class)
                .hasMessage("인증 번호가 일치하지 않습니다.");
        }
    }

    @Nested
    class requireVerified {

        @Test
        void SMS_인증된_상태에서_requireVerified를_호출하면_예외가_발생하지_않는다() {
            // given
            SMS_인증.verify(CORRECT_CODE);

            // when / then
            assertThatCode(SMS_인증::requireVerified)
                .doesNotThrowAnyException();
        }

        @Test
        void SMS_미인증_상태에서_requireVerified를_호출하면_AuthorizationException이_발생한다() {
            // when / then
            assertThatThrownBy(SMS_인증::requireVerified)
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("본인 인증 후 다시 시도해주십시오.");
        }

        @Test
        void 이메일_인증된_상태에서_requireVerified를_호출하면_예외가_발생하지_않는다() {
            // given
            이메일_인증.verify(CORRECT_CODE);

            // when / then
            assertThatCode(이메일_인증::requireVerified)
                .doesNotThrowAnyException();
        }

        @Test
        void 이메일_미인증_상태에서_requireVerified를_호출하면_AuthorizationException이_발생한다() {
            // when / then
            assertThatThrownBy(이메일_인증::requireVerified)
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("본인 인증 후 다시 시도해주십시오.");
        }
    }
}
