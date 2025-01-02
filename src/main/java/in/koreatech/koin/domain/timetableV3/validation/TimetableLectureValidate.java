package in.koreatech.koin.domain.timetableV3.validation;

import static lombok.AccessLevel.PRIVATE;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV3.exception.TimetableLectureTimeDuplicateException;
import in.koreatech.koin.domain.timetableV3.model.TimetableLectureInformation;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class TimetableLectureValidate {

    public static void checkDuplicateTimetableLectureTime(
        List<TimetableLecture> timetableLectures, List<TimetableLectureInformation> timetableLectureInformations
    ) {
        Queue<ClassInformation> classInformations = getClassInformations(timetableLectures);

        for (ClassInformation classInformation : classInformations) {
            for (TimetableLectureInformation timetableLectureInformation : timetableLectureInformations) {
                int startTime = timetableLectureInformation.getLectureInformation() != null ?
                    timetableLectureInformation.getLectureInformation().getStarTime() :
                    timetableLectureInformation.getStartTime();
                int endTime = timetableLectureInformation.getLectureInformation() != null ?
                    timetableLectureInformation.getLectureInformation().getEndTime() :
                    timetableLectureInformation.getEndTime();

                if (classInformation.checkBetweenTime(startTime, endTime)) {
                    throw new TimetableLectureTimeDuplicateException(
                        String.format("%s 강의 시간과 중복됩니다", classInformation.classTitle));
                }
            }
        }
    }

    private static Queue<ClassInformation> getClassInformations(List<TimetableLecture> timetableLectures) {
        Queue<ClassInformation> classInformations = new LinkedList<>();

        for (TimetableLecture timetableLecture : timetableLectures) {
            List<TimetableLectureInformation> timetableLectureInformations = timetableLecture.getTimetableLectureInformations();

            for (TimetableLectureInformation timetableLectureInformation : timetableLectureInformations) {
                int startTime = timetableLectureInformation.getLectureInformation() != null ?
                    timetableLectureInformation.getLectureInformation().getStarTime() :
                    timetableLectureInformation.getStartTime();
                int endTime = timetableLectureInformation.getLectureInformation() != null ?
                    timetableLectureInformation.getLectureInformation().getEndTime() :
                    timetableLectureInformation.getEndTime();

                classInformations.add(new ClassInformation(timetableLecture.getClassTitle(), startTime, endTime));
            }
        }
        return classInformations;
    }

    public record ClassInformation(
        String classTitle,
        int startTime,
        int endTime
    ) {
        public boolean checkBetweenTime(int startTime, int endTime) {
            return this.startTime <= startTime || this.endTime <= endTime;
        }
    }
}
