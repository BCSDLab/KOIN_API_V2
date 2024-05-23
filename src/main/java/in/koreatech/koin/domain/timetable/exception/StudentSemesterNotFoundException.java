package in.koreatech.koin.domain.timetable.exception;

import in.koreatech.koin.global.exception.DataNotFoundException;

public class StudentSemesterNotFoundException extends DataNotFoundException {

    private static final String DEFAULT_MESSAGE = "학생의 학기 정보가 없습니다.";

    public StudentSemesterNotFoundException(String message) {
        super(message);
    }

    public StudentSemesterNotFoundException(String message, String detail) {
        super(message, detail);
    }

    public static StudentSemesterNotFoundException withDetail(String detail) {
        return new StudentSemesterNotFoundException(DEFAULT_MESSAGE, detail);
    }
}
