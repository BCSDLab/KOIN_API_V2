package in.koreatech.koin.unit.fixutre;

import in.koreatech.koin._common.util.random.VerificationNumberGenerator;
import in.koreatech.koin.domain.user.verification.model.UserVerificationStatus;
import in.koreatech.koin.unit.domain.user.verification.mock.StubVerificationNumberHolder;

public final class UserVerificationStatusFixture {

    public static UserVerificationStatus SMS_인증(String phoneNumber, String code) {
        VerificationNumberGenerator verificationNumberGenerator = new StubVerificationNumberHolder(code);
        return UserVerificationStatus.ofSms(phoneNumber, verificationNumberGenerator);
    }

    public static UserVerificationStatus Email_인증(String email, String code) {
        VerificationNumberGenerator verificationNumberGenerator = new StubVerificationNumberHolder(code);
        return UserVerificationStatus.ofEmail(email, verificationNumberGenerator);
    }
}
