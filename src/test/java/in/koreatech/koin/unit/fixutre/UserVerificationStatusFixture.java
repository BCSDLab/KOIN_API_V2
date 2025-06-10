package in.koreatech.koin.unit.fixutre;

import in.koreatech.koin._common.util.random.VerificationNumberGenerator;
import in.koreatech.koin.domain.user.verification.model.UserVerificationStatus;
import in.koreatech.koin.unit.domain.user.verification.mock.TestVerificationNumberHolder;

public final class UserVerificationStatusFixture {

    public static UserVerificationStatus SMS_인증(String phoneNumber, String code) {
        VerificationNumberGenerator verificationNumberGenerator = new TestVerificationNumberHolder(code);
        return UserVerificationStatus.createBySms(phoneNumber, verificationNumberGenerator);
    }

    public static UserVerificationStatus 이메일_인증(String email, String code) {
        VerificationNumberGenerator verificationNumberGenerator = new TestVerificationNumberHolder(code);
        return UserVerificationStatus.createByEmail(email, verificationNumberGenerator);
    }
}
