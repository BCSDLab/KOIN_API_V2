package in.koreatech.koin.domain.timetableV2.factory;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.timetableV2.dto.response.TimetableFrameUpdateResponse;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import lombok.RequiredArgsConstructor;

/**
 * TODO. 
 *  1. 메소드 이름
 *  2. updater 삭제
 *  3. creator -> 팩토리 패턴에 적합한 방향, 삭제 유지 삭제, 생성만 하기
 *  4. dto 끊기 -> factory에서 dto를 반환 / 매개변수가 dto인 부분도 있음
 */
@Component
@RequiredArgsConstructor
public class TimetableFrameUpdater {

    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;

    public TimetableFrameUpdateResponse updateTimetableFrame(
        TimetableFrame timeTableFrame, Integer userId, String timetableName, boolean isMain
    ) {
        cancelIfMainTimetable(userId, timeTableFrame.getSemester().getId(), isMain);
        timeTableFrame.updateTimetableFrame(timeTableFrame.getSemester(), timetableName, isMain);
        return TimetableFrameUpdateResponse.from(timeTableFrame);
    }

    private void cancelIfMainTimetable(Integer userId, Integer semesterId, boolean isMain) {
        if (isMain) {
            TimetableFrame mainTimetableFrame = timetableFrameRepositoryV2.getMainTimetableByUserIdAndSemesterId(userId,
                semesterId);
            mainTimetableFrame.updateMainFlag(false);
        }
    }
}
