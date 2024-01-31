package in.koreatech.koin.acceptance;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.model.UserIdentity;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.StudentRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

class UserApiTest extends AcceptanceTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("올바른 학생계정인지 확인한다")
    void studentCheckMe() {
        Student student = Student.builder()
            .studentNumber("2019136135")
            .anonymousNickname("익명")
            .department("컴퓨터공학부")
            .userIdentity(UserIdentity.UNDERGRADUATE)
            .isGraduated(false)
            .user(
                User.builder()
                    .password("1234")
                    .nickname("주노")
                    .name("최준호")
                    .phoneNumber("010-1234-5678")
                    .userType(UserType.STUDENT)
                    .gender(UserGender.MAN)
                    .email("test@koreatech.ac.kr")
                    .isAuthed(true)
                    .isDeleted(false)
                    .build()
            )
            .build();

        studentRepository.save(student);
        String token = jwtProvider.createToken(student.getUser());

        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .header("Authorization", "Bearer " + token)
            .when()
            .log().all()
            .get("/user/student/me")
            .then()
            .log().all()
            .statusCode(HttpStatus.OK.value())
            .extract();

        User user = student.getUser();

        assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getString("anonymous_nickname"))
                    .isEqualTo(student.getAnonymousNickname());
                softly.assertThat(response.body().jsonPath().getString("email"))
                    .isEqualTo(user.getEmail());
                softly.assertThat(response.body().jsonPath().getString("gender"))
                    .isEqualTo(user.getGender().name());
                softly.assertThat(response.body().jsonPath().getString("major"))
                    .isEqualTo(student.getDepartment());
                softly.assertThat(response.body().jsonPath().getString("name"))
                    .isEqualTo(user.getName());
                softly.assertThat(response.body().jsonPath().getString("nickname"))
                    .isEqualTo(user.getNickname());
                softly.assertThat(response.body().jsonPath().getString("phone_number"))
                    .isEqualTo(user.getPhoneNumber());
                softly.assertThat(response.body().jsonPath().getString("student_number"))
                    .isEqualTo(student.getStudentNumber());
            }
        );
    }

    @Test
    @DisplayName("올바른 학생계정인지 확인한다 - 토큰 정보가 올바르지 않으면  401")
    void studentCheckMeUnAuthorized() {
        Student student = Student.builder()
            .studentNumber("2019136135")
            .anonymousNickname("익명")
            .department("컴퓨터공학부")
            .userIdentity(UserIdentity.UNDERGRADUATE)
            .isGraduated(false)
            .user(
                User.builder()
                    .password("1234")
                    .nickname("주노")
                    .name("최준호")
                    .phoneNumber("010-1234-5678")
                    .userType(UserType.STUDENT)
                    .gender(UserGender.MAN)
                    .email("test@koreatech.ac.kr")
                    .isAuthed(true)
                    .isDeleted(false)
                    .build()
            )
            .build();

        studentRepository.save(student);
        String token = "invalidToken";

        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .header("Authorization", "Bearer " + token)
            .when()
            .log().all()
            .get("/user/student/me")
            .then()
            .log().all()
            .statusCode(HttpStatus.UNAUTHORIZED.value())
            .extract();
    }

    @Test
    @DisplayName("올바른 학생계정인지 확인한다 - 회원을 찾을 수 없으면 404")
    void studentCheckMeNotFound() {
        User user = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(UserType.STUDENT)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        userRepository.save(user);
        String token = jwtProvider.createToken(user);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .log().all()
            .header("Authorization", "Bearer " + token)
            .when()
            .log().all()
            .get("/user/student/me")
            .then()
            .log().all()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .extract();
    }
}
