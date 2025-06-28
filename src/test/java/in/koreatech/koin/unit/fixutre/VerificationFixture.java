package in.koreatech.koin.unit.fixutre;

import in.koreatech.koin._common.util.random.VerificationNumberGenerator;
import in.koreatech.koin.domain.user.verification.config.VerificationProperties;
import in.koreatech.koin.domain.user.verification.model.VerificationCount;
import in.koreatech.koin.domain.user.verification.model.VerificationCode;
import in.koreatech.koin.unit.domain.user.verification.mock.StubVerificationNumberHolder;

public class VerificationFixture {

    public static VerificationCode SMS_인증_코드(String phoneNumber, String code) {
        VerificationNumberGenerator verificationNumberGenerator = new StubVerificationNumberHolder(code);
        return VerificationCode.ofSms(phoneNumber, verificationNumberGenerator);
    }

    public static VerificationCode Email_인증_코드(String email, String code) {
        VerificationNumberGenerator verificationNumberGenerator = new StubVerificationNumberHolder(code);
        return VerificationCode.ofEmail(email, verificationNumberGenerator);
    }

    public static VerificationCount SMS_인증_횟수(String phoneNumber) {
        VerificationProperties verificationProperties = new VerificationProperties(5);
        return VerificationCount.of(phoneNumber, verificationProperties);
    }

    public static VerificationCount Email_인증_횟수(String email) {
        VerificationProperties verificationProperties = new VerificationProperties(5);
        return VerificationCount.of(email, verificationProperties);
    }
}
