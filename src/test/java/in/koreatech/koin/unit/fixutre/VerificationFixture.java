package in.koreatech.koin.unit.fixutre;

import in.koreatech.koin._common.util.random.VerificationNumberGenerator;
import in.koreatech.koin.domain.user.verification.model.UserDailyVerificationCount;
import in.koreatech.koin.domain.user.verification.model.UserVerificationStatus;
import in.koreatech.koin.unit.domain.user.verification.mock.StubVerificationNumberHolder;

public class VerificationFixture {

    public static UserVerificationStatus SMS_인증_상태(String phoneNumber, String code) {
        VerificationNumberGenerator verificationNumberGenerator = new StubVerificationNumberHolder(code);
        return UserVerificationStatus.ofSms(phoneNumber, verificationNumberGenerator);
    }

    public static UserVerificationStatus Email_인증_상태(String email, String code) {
        VerificationNumberGenerator verificationNumberGenerator = new StubVerificationNumberHolder(code);
        return UserVerificationStatus.ofEmail(email, verificationNumberGenerator);
    }

    public static UserDailyVerificationCount SMS_인증_횟수(String phoneNumber) {
        return UserDailyVerificationCount.from(phoneNumber);
    }

    public static UserDailyVerificationCount Email_인증_횟수(String email) {
        return UserDailyVerificationCount.from(email);
    }
}
