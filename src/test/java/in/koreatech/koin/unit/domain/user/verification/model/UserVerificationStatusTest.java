package in.koreatech.koin.unit.domain.user.verification.model;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

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

    private static final String TEST_EMAIL = "user@koreatech.ac.kr";
    private static final String TEST_PHONE_NUMBER = "01012345678";
    private static final String CORRECT_CODE = "123456";
    private static final String WRONG_CODE = "999999";

    @Nested
    @DisplayName("인증 유효시간 테스트")
    class CreateTest {

        @Test
        @DisplayName("SMS 인증용 객체의 유효시간은 3분이다")
        void createBySms() {
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
        @DisplayName("Email 인증용 객체의 유효시간은 5분이다")
        void createByEmail() {
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
    @DisplayName("인증 로직 테스트")
    class VerifyTest {

        @Test
        @DisplayName("올바른 인증번호로 인증에 성공하면, `verified` 상태가 true가 되고 유효시간이 연장된다")
        void verifySuccess() {
            // given
            UserVerificationStatus verificationStatus = UserVerificationStatusFixture.휴대폰_인증_상태(CORRECT_CODE, TEST_PHONE_NUMBER);
            long expectedExpiration = 60 * 60L;

            // when
            verificationStatus.verify(CORRECT_CODE);

            // then
            assertThat(verificationStatus.isVerified()).isTrue();
            assertThat(verificationStatus.getExpiration()).isEqualTo(expectedExpiration);
        }

        @Test
        @DisplayName("잘못된 인증번호를 입력하면 KoinIllegalArgumentException이 발생한다")
        void verifyFail() {
            // given
            UserVerificationStatus verificationStatus = UserVerificationStatusFixture.휴대폰_인증_상태(CORRECT_CODE, TEST_PHONE_NUMBER);

            // when / then
            assertThatThrownBy(() -> verificationStatus.verify(WRONG_CODE))
                .isInstanceOf(KoinIllegalArgumentException.class)
                .hasMessage("인증 번호가 일치하지 않습니다.");
        }
    }

    @Nested
    @DisplayName("인증 상태 확인 테스트")
    class RequireVerifiedTest {

        @Test
        @DisplayName("인증된 상태에서 호출하면 예외가 발생하지 않는다")
        void requireVerifiedSuccess() {
            // given
            UserVerificationStatus verificationStatus = UserVerificationStatusFixture.휴대폰_인증_상태(CORRECT_CODE, TEST_PHONE_NUMBER);
            verificationStatus.verify(CORRECT_CODE);

            // when / then
            assertThatCode(verificationStatus::requireVerified)
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("미인증 상태에서 호출하면 AuthorizationException이 발생한다")
        void requireVerifiedFail() {
            // given
            UserVerificationStatus verificationStatus = UserVerificationStatusFixture.휴대폰_인증_상태(CORRECT_CODE, TEST_PHONE_NUMBER);

            // when / then
            assertThatThrownBy(verificationStatus::requireVerified)
                .isInstanceOf(AuthorizationException.class)
                .hasMessage("본인 인증 후 다시 시도해주십시오.");
        }
    }
}
