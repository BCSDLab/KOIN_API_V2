package in.koreatech.koin.domain.timetableV2.validation;

import java.util.Objects;

import in.koreatech.koin.global.auth.exception.AuthorizationException;

public class TimetableFrameValidate {
    public static void validateUserAuthorization(Integer frameUserId, Integer userId) {
        if (!Objects.equals(frameUserId, userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }
    }
}
