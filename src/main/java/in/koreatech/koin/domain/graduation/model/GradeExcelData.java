package in.koreatech.koin.domain.graduation.model;

import static in.koreatech.koin.global.code.ApiResponseCode.INVALID_SEMESTER_REGEX;

import in.koreatech.koin.global.exception.CustomException;

public record GradeExcelData(
    String year,
    String semester,
    String code,
    String classTitle,
    String lectureClass,
    String professor,
    String courseType,
    String credit,
    String grade,
    String retakeStatus
) {

    private static final String MIDDLE_TOTAL = "소 계";
    private static final String FAIL = "F";
    private static final String UNSATISFACTORY = "U";
    private static final String TOTAL = "합 계";
    private static final String FIRST_SEMESTER = "1";
    private static final String SECOND_SEMESTER = "2";
    private static final String SUMMER_SEMESTER = "동계";
    private static final String WINER_SEMESTER = "여름";

    public boolean isSkipRow() {
        return classTitle.equals(MIDDLE_TOTAL) ||
            grade.equals(FAIL) ||
            grade.equals(UNSATISFACTORY);
    }

    public boolean isTotalRow() {
        return classTitle.equals(TOTAL);
    }

    public String getKoinSemester() {
        return switch (semester) {
            case FIRST_SEMESTER, SECOND_SEMESTER -> year + semester;
            case WINER_SEMESTER -> year + "-" + "겨울";
            case SUMMER_SEMESTER -> year + "-" + "여름";
            default -> throw CustomException.of(INVALID_SEMESTER_REGEX);
        };
    }
}
