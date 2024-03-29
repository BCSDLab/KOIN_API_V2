package in.koreatech.koin.acceptance;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.model.UserIdentity;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import in.koreatech.koin.domain.user.repository.StudentRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

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
                    .userType(STUDENT)
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
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/user/student/me")
            .then()
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
                    .userType(STUDENT)
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
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/user/student/me")
            .then()
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
            .userType(STUDENT)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        userRepository.save(user);
        String token = jwtProvider.createToken(user);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/user/student/me")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .extract();
    }

    @Test
    @DisplayName("회원이 탈퇴한다")
    void userWithdraw() {
        User user = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        userRepository.save(user);
        String token = jwtProvider.createToken(user);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .delete("/user")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        assertThat(userRepository.findById(user.getId())).isNotPresent();
    }

    @Test
    @DisplayName("이메일이 중복인지 확인한다")
    void emailCheckExists() {
        String email = "test@koreatech.ac.kr";

        RestAssured
            .given()
            .param("address", email)
            .when()
            .get("/user/check/email")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertThat(userRepository.findByEmail(email)).isNotPresent();
    }

    @Test
    @DisplayName("이메일이 중복인지 확인한다 - 이메일을 보내지 않으면 400")
    void emailCheckExistsNull() {
        RestAssured
            .when()
            .get("/user/check/email")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract();
    }

    @Test
    @DisplayName("이메일이 중복인지 확인한다 - 잘못된 이메일 형식이면 400")
    void emailCheckExistsWrongFormat() {
        String email = "wrong email format";

        RestAssured
            .given()
            .param("address", email)
            .when()
            .get("/user/check/email")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract();
    }

    @Test
    @DisplayName("이메일이 중복인지 확인한다 - 중복이면 422")
    void emailCheckExistsAlreadyExists() {
        User user = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        userRepository.save(user);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .param("address", user.getEmail())
            .when()
            .get("/user/check/email")
            .then()
            .statusCode(HttpStatus.CONFLICT.value())
            .extract();

        assertThat(response.body().jsonPath().getString("message"))
            .isEqualTo("이미 존재하는 데이터입니다.");
    }

    @Test
    @DisplayName("닉네임 중복일때 상태코드 409를 반환한다.")
    void checkDuplicationOfNicknameConflict() {
        User user = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        userRepository.save(user);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .param("nickname", user.getNickname())
            .get("/user/check/nickname")
            .then()
            .statusCode(HttpStatus.CONFLICT.value())
            .extract();

        assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getString("message")).isEqualTo("이미 존재하는 데이터입니다.");
            }
        );
    }

    @Test
    @DisplayName("닉네임 중복이 아닐시 상태코드 200을 반환한다.")
    void checkDuplicationOfNickname() {
        User user = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        userRepository.save(user);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .when()
            .param("nickname", "철수")
            .get("/user/check/nickname")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();
    }

    @Test
    @DisplayName("닉네임 제약조건 위반시 상태코드 400를 반환한다.")
    void checkDuplicationOfNicknameBadRequest() {
        User user = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        userRepository.save(user);

        RestAssured
            .given()
            .when()
            .param("nickname", "철".repeat(11))
            .get("/user/check/nickname")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract();

        RestAssured
            .given()
            .when()
            .param("nickname", "")
            .get("/user/check/nickname")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract();
    }

    @Test
    @DisplayName("알림 구독여부 조회 - 구독중")
    void getNotificationCheck() {
        User user = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .deviceToken("deviceToken")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        userRepository.save(user);
        String token = jwtProvider.createToken(user);

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/user/notification")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertThat(response.jsonPath().getBoolean("isPermit")).isTrue();
    }

    @Test
    @DisplayName("알림 구독여부 조회 - 구독중이 아님")
    void getNotificationCheckNotSubscribe() {
        User user = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        userRepository.save(user);
        String token = jwtProvider.createToken(user);

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/user/notification")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        assertThat(response.jsonPath().getBoolean("isPermit")).isFalse();
    }

    @Test
    @DisplayName("알림 구독")
    void subscribeNotification() {
        User user = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        userRepository.save(user);
        String token = jwtProvider.createToken(user);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .when()
            .body("""
                {
                    "deviceToken": "deviceTokenForSubscribe"
                }
                """)
            .post("/user/notification")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        var userResponse = userRepository.getById(user.getId());
        assertThat(userResponse.getDeviceToken()).isEqualTo("deviceTokenForSubscribe");
    }

    @Test
    @DisplayName("알림 구독 취소")
    void unsubscribeNotification() {
        User user = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .deviceToken("deviceToken")
            .isAuthed(true)
            .isDeleted(false)
            .build();

        userRepository.save(user);
        String token = jwtProvider.createToken(user);

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .when()
            .delete("/user/notification")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        var userResponse = userRepository.getById(user.getId());
        assertThat(userResponse.getDeviceToken()).isNull();
    }
}
