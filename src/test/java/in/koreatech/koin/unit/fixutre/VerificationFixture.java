package in.koreatech.koin.unit.fixutre;

import in.koreatech.koin.common.util.random.VerificationNumberGenerator;
import in.koreatech.koin.domain.user.verification.model.VerificationChannel;
import in.koreatech.koin.domain.user.verification.model.VerificationCode;
import in.koreatech.koin.domain.user.verification.model.VerificationCount;
import in.koreatech.koin.unit.domain.user.verification.mock.StubVerificationNumberHolder;

public class VerificationFixture {

    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int MAX_VERIFICATION_COUNT = 5;

    public static VerificationCode SMS_인증_코드(String phoneNumber, String code) {
        VerificationNumberGenerator verificationNumberGenerator = new StubVerificationNumberHolder(code);
        return VerificationCode.of(phoneNumber, verificationNumberGenerator, VerificationChannel.SMS);
    }

    public static VerificationCode Email_인증_코드(String email, String code) {
        VerificationNumberGenerator verificationNumberGenerator = new StubVerificationNumberHolder(code);
        return VerificationCode.of(email, verificationNumberGenerator, VerificationChannel.EMAIL);
    }

    public static VerificationCount SMS_인증_횟수(String phoneNumber) {
        return VerificationCount.of(phoneNumber, IP_ADDRESS, MAX_VERIFICATION_COUNT);
    }

    public static VerificationCount Email_인증_횟수(String email) {
        return VerificationCount.of(email, IP_ADDRESS, MAX_VERIFICATION_COUNT);
    }
}
