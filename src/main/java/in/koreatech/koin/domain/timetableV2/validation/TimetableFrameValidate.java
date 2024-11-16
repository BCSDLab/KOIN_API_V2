package in.koreatech.koin.domain.timetableV2.validation;

import java.util.Objects;

import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;

public class TimetableFrameValidate {
    public static void validateUserAuthorization(Integer frameUserId, Integer userId) {
        if (!Objects.equals(frameUserId, userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }
    }

    public static void validateTimetableFrameUpdate(TimetableFrame timeTableFrame, boolean isMain) {
        if (timeTableFrame.isMain() && !isMain) {
            throw new KoinIllegalArgumentException("메인 시간표는 필수입니다.");
        }
    }
}
