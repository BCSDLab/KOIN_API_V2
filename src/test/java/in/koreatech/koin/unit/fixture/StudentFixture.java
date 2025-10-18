package in.koreatech.koin.unit.fixture;

import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.user.model.User;

import static in.koreatech.koin.domain.user.model.UserGender.MAN;
import static in.koreatech.koin.domain.user.model.UserIdentity.UNDERGRADUATE;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

public class StudentFixture {

    private StudentFixture() {}

    public static Student 준호_학생(Department department, Major major) {
        return Student.builder()
            .studentNumber("2019136135")
            .department(department)
            .major(major)
            .userIdentity(UNDERGRADUATE)
            .isGraduated(false)
            .user(
                User.builder()
                    .name("테스트용_준호")
                    .nickname("준호")
                    .anonymousNickname("익명")
                    .phoneNumber("01012345678")
                    .email("juno@koreatech.ac.kr")
                    .loginId("test_id")
                    .loginPw("test_pw")
                    .userType(STUDENT)
                    .gender(MAN)
                    .isAuthed(true)
                    .isDeleted(false)
                    .build()
            )
            .build();
    }

    public static Student 익명_학생(Department department) {
        return Student.builder()
            .studentNumber("2020136111")
            .department(department)
            .userIdentity(UNDERGRADUATE)
            .isGraduated(false)
            .user(
                User.builder()
                    .name("테스트용_익명")
                    // nickname 생략
                    .anonymousNickname("익명")
                    .phoneNumber("01011111111")
                    .email("lyw4888@koreatech.ac.kr")
                    .loginId("test_id")
                    .loginPw("test_pw")
                    .userType(STUDENT)
                    .gender(MAN)
                    .isAuthed(true)
                    .isDeleted(false)
                    .build()
            )
            .build();
    }

    public static Student 익명_학생(Integer id, Department department) {
        return Student.builder()
            .id(id)
            .studentNumber("2020136111")
            .department(department)
            .userIdentity(UNDERGRADUATE)
            .isGraduated(false)
            .user(
                User.builder()
                    .name("테스트용_익명")
                    // nickname 생략
                    .anonymousNickname("익명")
                    .phoneNumber("01011111111")
                    .email("lyw4888@koreatech.ac.kr")
                    .loginId("test_id")
                    .loginPw("test_pw")
                    .userType(STUDENT)
                    .gender(MAN)
                    .isAuthed(true)
                    .isDeleted(false)
                    .build()
            )
            .build();
    }

    public static Student 성빈_학생(Department department) {
        return Student.builder()
            .studentNumber("2023100514")
            .department(department)
            .userIdentity(UNDERGRADUATE)
            .isGraduated(false)
            .user(
                User.builder()
                    .name("박성빈")
                    .nickname("빈")
                    .anonymousNickname("익명")
                    .phoneNumber("01099411123")
                    .email("testsungbeen@koreatech.ac.kr")
                    .loginId("test_id")
                    .loginPw("test_pw")
                    .userType(STUDENT)
                    .gender(MAN)
                    .isAuthed(true)
                    .isDeleted(false)
                    .build()
            )
            .build();
    }
}
