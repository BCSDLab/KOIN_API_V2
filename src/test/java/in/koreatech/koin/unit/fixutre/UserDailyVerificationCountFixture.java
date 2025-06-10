package in.koreatech.koin.unit.fixutre;

import in.koreatech.koin.domain.user.verification.model.UserDailyVerificationCount;

public class UserDailyVerificationCountFixture {

    public static UserDailyVerificationCount SMS_인증_횟수(String phoneNumber) {
        return UserDailyVerificationCount.create(phoneNumber);
    }

    public static UserDailyVerificationCount 이메일_인증_횟수(String email) {
        return UserDailyVerificationCount.create(email);
    }
}
