package in.koreatech.koin.unit.fixutre;

import in.koreatech.koin._common.util.random.VerificationNumberGenerator;
import in.koreatech.koin.domain.user.verification.model.UserVerificationStatus;
import in.koreatech.koin.unit.domain.user.verification.mock.FakeVerificationNumberGenerator;

public final class UserVerificationStatusFixture {

    public static UserVerificationStatus 휴대폰_인증_상태(String code, String phoneNumber) {
        VerificationNumberGenerator verificationNumberGenerator = new FakeVerificationNumberGenerator(code);
        return UserVerificationStatus.createBySms(phoneNumber, verificationNumberGenerator);
    }
}
