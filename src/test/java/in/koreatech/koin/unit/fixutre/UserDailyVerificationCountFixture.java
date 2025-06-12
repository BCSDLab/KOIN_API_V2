package in.koreatech.koin.unit.fixutre;

import in.koreatech.koin.domain.user.verification.model.UserDailyVerificationCount;

public class UserDailyVerificationCountFixture {

    public static UserDailyVerificationCount SMS_인증_횟수(String phoneNumber) {
        return UserDailyVerificationCount.from(phoneNumber);
    }

    public static UserDailyVerificationCount Email_인증_횟수(String email) {
        return UserDailyVerificationCount.from(email);
    }
}
