package in.koreatech.koin.domain.timetableV2.util;

import static in.koreatech.koin.domain.timetableV2.validation.TimetableFrameValidate.validateTimetableFrameUpdate;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.timetableV2.dto.response.TimetableFrameUpdateResponse;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TimetableUpdater {

    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;

    public TimetableFrameUpdateResponse updateTimetableFrame(
        TimetableFrame timeTableFrame, Integer userId, String timetableName, boolean isMain
    ) {
        validateTimetableFrameUpdate(timeTableFrame, isMain);
        cancelMainTimetable(userId, timeTableFrame.getSemester().getId(), isMain);
        timeTableFrame.updateTimetableFrame(timeTableFrame.getSemester(), timetableName, isMain);
        return TimetableFrameUpdateResponse.from(timeTableFrame);
    }

    private void cancelMainTimetable(Integer userId, Integer semesterId, boolean isMain) {
        if (isMain) {
            TimetableFrame mainTimetableFrame = timetableFrameRepositoryV2.getMainTimetableByUserIdAndSemesterId(userId,
                semesterId);
            mainTimetableFrame.updateMainFlag(false);
        }
    }
}
