package in.koreatech.koin.domain.timetableV2.util;

import java.util.List;

import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;

public class GradeCalculator {
    private GradeCalculator() {
    }

    public static int calculateGrades(TimetableFrame timetableFrame, List<TimetableLecture> timetableLectures) {
        if (timetableFrame.isMain()) {
            return calculateGrades(timetableLectures);
        }
        return 0;
    }

    public static int calculateTotalGrades(List<TimetableFrame> timetableFrames) {
        return timetableFrames.stream()
            .mapToInt(timetableFrame -> calculateGrades(timetableFrame.getTimetableLectures()))
            .sum();
    }

    private static int calculateGrades(List<TimetableLecture> timetableLectures) {
        return timetableLectures.stream()
            .mapToInt(lecture -> {
                if (lecture.getLecture() != null) {
                    return Integer.parseInt(lecture.getLecture().getGrades());
                }
                return Integer.parseInt(lecture.getGrades());
            })
            .sum();
    }
}
