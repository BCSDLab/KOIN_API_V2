package in.koreatech.koin.admin.lecture.model;

public record LectureKey(
    String code,
    String lectureClass
) {

    public static LectureKey of(String code, String lectureClass) {
        return new LectureKey(code, lectureClass);
    }
}
