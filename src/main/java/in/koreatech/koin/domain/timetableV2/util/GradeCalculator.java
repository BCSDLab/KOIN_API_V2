package in.koreatech.koin.domain.timetableV2.util;

import java.util.List;

import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;

public class GradeCalculator {
    private GradeCalculator() {
    }

    public static int calculateGrades(TimetableFrame timetableFrame) {
        if (timetableFrame.isMain()) {
            return calculateGrades(timetableFrame.getTimetableLectures());
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
            .mapToInt(GradeCalculator::determineGrade)
            .sum();
    }

    private static Integer determineGrade(TimetableLecture timetableLecture) {
        if (timetableLecture.getLecture() != null) {
            return Integer.parseInt(timetableLecture.getLecture().getGrades());
        }
        return Integer.parseInt(timetableLecture.getGrades());
    }
}
