package in.koreatech.koin.domain.timetableV2.validation;

import static lombok.AccessLevel.PRIVATE;

import java.util.Objects;

import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin._common.auth.exception.AuthorizationException;
import in.koreatech.koin._common.exception.custom.KoinIllegalArgumentException;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class TimetableFrameValidate {

    public static void validateUserOwnsFrame(Integer frameUserId, Integer userId) {
        if (!Objects.equals(frameUserId, userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }
    }

    public static void validateMainTimetableRequired(TimetableFrame timeTableFrame, boolean isMain) {
        if (timeTableFrame.isMain() && !isMain) {
            throw new KoinIllegalArgumentException("메인 시간표는 필수입니다.");
        }
    }
}
