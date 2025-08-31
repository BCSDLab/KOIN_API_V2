package in.koreatech.koin.domain.graduation.model;

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

    public boolean shouldBeSkipped() {
        return classTitle.equals(MIDDLE_TOTAL) ||
            grade.equals(FAIL) ||
            grade.equals(UNSATISFACTORY);
    }

    public boolean isTotal() {
        return classTitle.equals(TOTAL);
    }
}
