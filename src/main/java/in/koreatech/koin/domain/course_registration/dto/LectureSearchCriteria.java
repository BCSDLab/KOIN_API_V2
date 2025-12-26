package in.koreatech.koin.domain.course_registration.dto;

public record LectureSearchCriteria(
    String name,
    String department,
    Integer year,
    String semester
) {

    /**
     *  년도와 학기를 조합하여 semester_date 형식으로 변환
     *  2025년 1학기 -> "20251"
     *  2025년 겨울학기 -> "2025-겨울"
     */
    public String getSemesterDate() {
        if (year == null || semester == null) {
            return null;
        }

        return switch (semester) {
            case "1학기" -> year + "1";
            case "여름학기" -> year + "-여름";
            case "2학기" -> year + "2";
            case "겨울학기" -> year + "-겨울";
            default -> null;
        };
    }
}
