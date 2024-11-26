package in.koreatech.koin.domain.timetableV2.factory;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV2.dto.request.TimetableFrameCreateRequest;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.user.model.User;

@Component
public class TimetableFrameCreator {

    private static final String DEFAULT_TIMETABLE_FRAME_NAME = "시간표 %d";

    public TimetableFrame createTimetableFrame(
        TimetableFrameCreateRequest request, User user, Semester semester, int currentFrameCount
    ) {
        boolean isMain = isDetermineMain(currentFrameCount);
        String name = determineTimetableName(request.timetableName(), currentFrameCount);
        return request.toTimetablesFrame(user, semester, name, isMain);
    }

    private boolean isDetermineMain(int currentFrameCount) {
        return currentFrameCount == 0;
    }

    private String determineTimetableName(String requestedName, int currentFrameCount) {
        /*
            TODO
            - 공백인 경우가 있으면?
            - isBlank~~ 이런 메소드 만들어서 사용하기
            - if(requestName == null || requestName.isBlank)
         */
        if (requestedName != null) {
            return requestedName;
        }
        return getDefaultTimetableFrameName(currentFrameCount + 1);
    }

    private String getDefaultTimetableFrameName(int currentFrameCount) {
        return String.format(DEFAULT_TIMETABLE_FRAME_NAME, currentFrameCount);
    }
}
