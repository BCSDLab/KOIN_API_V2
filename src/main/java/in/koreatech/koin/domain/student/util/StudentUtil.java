package in.koreatech.koin.domain.student.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StudentUtil {

    public static Integer parseStudentNumberYear(String studentNumber) {
        return Integer.parseInt(studentNumber.substring(0, 4));
    }

    public static String parseStudentNumberYearAsString(String studentNumber) {
        return studentNumber.substring(0, 4);
    }
}
