package in.koreatech.koin.domain.student.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StudentUtil {

    public static Integer parseStudentNumberYear(String studentNumber) {
        return (studentNumber != null) ? Integer.parseInt(studentNumber.substring(0, 4)) : null;
    }

    public static String parseStudentNumberYearAsString(String studentNumber) {
        return (studentNumber != null) ? studentNumber.substring(0, 4) : null;
    }
}
