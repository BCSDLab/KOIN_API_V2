package in.koreatech.koin.acceptance;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.ZonedDateTime;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.dept.model.Dept;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.model.UserIdentity;
import in.koreatech.koin.domain.user.repository.StudentRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;

class UserApiTest extends AcceptanceTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void setUp() {
        when(clock.instant()).thenReturn(
            ZonedDateTime.parse("2024-02-21 18:00:00 KST", ofPattern("yyyy-MM-dd " + "HH:mm:ss z")).toInstant());
        when(clock.getZone()).thenReturn(Clock.systemDefaultZone().getZone());
    }

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
                softly.assertThat(response.body().jsonPath().getInt("gender"))
                    .isEqualTo(user.getGender().ordinal());
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
    @DisplayName("학생이 정보를 수정한다")
    void studentUpdateMe() {
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
            .contentType(ContentType.JSON)
            .body("""
                  {
                    "gender" : 1,
                    "major" : "기계공학부",
                    "name" : "서정빈",
                    "nickname" : "duehee",
                    "phone_number" : "010-2345-6789",
                    "student_number" : "2019136136"
                  }
                """)
            .when()
            .put("/user/student/me")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();
    }

    @Test
    @DisplayName("학생이 정보를 수정한다 - 학번의 형식이 맞지 않으면 400")
    void studentUpdateMeNotValidStudentNumber() {
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
            .contentType(ContentType.JSON)
            .body("""
                  {
                    "gender" : 0,
                    "major" : "메카트로닉스공학부",
                    "name" : "최주노",
                    "nickname" : "juno",
                    "phone_number" : "010-2345-6789",
                    "student_number" : "201913613"
                  }
                """)
            .when()
            .put("/user/student/me")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract();
    }

    @Test
    @DisplayName("학생이 정보를 수정한다 - 학부의 형식이 맞지 않으면 400")
    void studentUpdateMeNotValidDepartment() {
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
            .contentType(ContentType.JSON)
            .body("""
                  {
                    "gender" : 0,
                    "major" : "경영학과",
                    "name" : "최주노",
                    "nickname" : "juno",
                    "phone_number" : "010-2345-6789",
                    "student_number" : "2019136136"
                  }
                """)
            .when()
            .put("/user/student/me")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value())
            .extract();
    }

    @Test
    @DisplayName("학생이 정보를 수정한다 - 토큰이 올바르지 않다면 401")
    void studentUpdateMeUnAuthorized() {
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
            .contentType(ContentType.JSON)
            .body("""
                  {
                    "gender" : 0,
                    "major" : "메카트로닉스공학부",
                    "name" : "최주노",
                    "nickname" : "juno",
                    "phone_number" : "010-2345-6789",
                    "student_number" : "2019136136"
                  }
                """)
            .when()
            .put("/user/student/me")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value())
            .extract();
    }

    @Test
    @DisplayName("학생이 정보를 수정한다 - 회원을 찾을 수 없다면 404")
    void studentUpdateMeNotFound() {
        User user =
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
                .build();

        userRepository.save(user);
        String token = jwtProvider.createToken(user);

        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
                  {
                    "gender" : 0,
                    "major" : "메카트로닉스공학부",
                    "name" : "최주노",
                    "nickname" : "juno",
                    "phone_number" : "010-2345-6789",
                    "student_number" : "2019136136"
                  }
                """)
            .when()
            .put("/user/student/me")
            .then()
            .statusCode(HttpStatus.NOT_FOUND.value())
            .extract();
    }

    @Test
    @DisplayName("학생이 정보를 수정한다 - 이미 있는 닉네임이라면 409")
    void studentUpdateMeDuplicationNickname() {
        Student student1 = Student.builder()
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
                    .email("test1@koreatech.ac.kr")
                    .isAuthed(true)
                    .isDeleted(false)
                    .build()
            )
            .build();

        Student student2 = Student.builder()
            .studentNumber("2020136065")
            .anonymousNickname("익명")
            .department("컴퓨터공학부")
            .userIdentity(UserIdentity.UNDERGRADUATE)
            .isGraduated(false)
            .user(
                User.builder()
                    .password("1234")
                    .nickname("duehee")
                    .name("서정빈")
                    .phoneNumber("010-1234-5678")
                    .userType(STUDENT)
                    .gender(UserGender.MAN)
                    .email("test2@koreatech.ac.kr")
                    .isAuthed(true)
                    .isDeleted(false)
                    .build()
            )
            .build();

        studentRepository.save(student1);
        studentRepository.save(student2);
        String token = jwtProvider.createToken(student1.getUser());

        ExtractableResponse<Response> response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
                 {
                    "gender" : 0,
                    "major" : "테스트학과",
                    "name" : "최주노",
                    "nickname" : "duehee",
                    "phone_number" : "010-2345-6789",
                    "student_number" : "2019136136"
                 }
                """)
            .when()
            .put("/user/student/me")
            .then()
            .statusCode(HttpStatus.CONFLICT.value())
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
            .contains("존재하는 이메일입니다.");
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
                softly.assertThat(response.body().jsonPath().getString("message"))
                    .contains("이미 존재하는 닉네임입니다.");
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
    @DisplayName("로그인된 사용자의 권한을 조회한다.")
    void getAuth() {
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
            .get("/user/auth")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        User user = student.getUser();

        assertSoftly(
            softly -> {
                softly.assertThat(response.body().jsonPath().getString("user_type")).isEqualTo(user.getUserType().getValue());
            }
        );
    }

    @Test
    @DisplayName("학생 회원가입 후 학교 이메일요청 이벤트가 발생한다.")
    void studentRegister() {
        RestAssured
            .given()
            .body("""
                {
                  "major": "컴퓨터공학부",
                  "email": "koko123@koreatech.ac.kr",
                  "name": "김철수",
                  "password": "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268",
                  "nickname": "koko",
                  "gender": "0",
                  "is_graduated": false,
                  "student_number": "2021136012",
                  "phone_number": "010-0000-0000"
                }
                """)
            .contentType(ContentType.JSON)
            .when()
            .post("/user/student/register")
            .then()
            .statusCode(HttpStatus.OK.value());

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                User user = userRepository.getByEmail("koko123@koreatech.ac.kr");
                Student student = studentRepository.getById(user.getId());

                assertSoftly(
                    softly -> {
                        softly.assertThat(student).isNotNull();
                        softly.assertThat(student.getUser().getNickname()).isEqualTo("koko");
                        softly.assertThat(student.getUser().getName()).isEqualTo("김철수");
                        softly.assertThat(student.getUser().getPhoneNumber()).isEqualTo("010-0000-0000");
                        softly.assertThat(student.getUser().getUserType()).isEqualTo(STUDENT);
                        softly.assertThat(student.getUser().getEmail()).isEqualTo("koko123@koreatech.ac.kr");
                        softly.assertThat(student.getUser().isAuthed()).isEqualTo(false);
                        softly.assertThat(student.getStudentNumber()).isEqualTo("2021136012");
                        softly.assertThat(student.getDepartment()).isEqualTo(Dept.COMPUTER_SCIENCE.getName());
                        softly.assertThat(student.getAnonymousNickname()).isNotNull();
                        verify(studentEventListener).onStudentEmailRequest(any());
                    }
                );
            }
        });
    }

    @Test
    @DisplayName("이메일 요청을 확인 후 회원가입 이벤트가 발생한다.")
    void authenticate() {
        RestAssured
            .given()
            .body("""
                {
                  "major": "컴퓨터공학부",
                  "email": "koko123@koreatech.ac.kr",
                  "name": "김철수",
                  "password": "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268",
                  "nickname": "koko",
                  "gender": "0",
                  "is_graduated": false,
                  "student_number": "2021136012",
                  "phone_number": "010-0000-0000"
                }
                """)
            .contentType(ContentType.JSON)
            .when()
            .post("/user/student/register")
            .then()
            .statusCode(HttpStatus.OK.value());

        User user = userRepository.getByEmail("koko123@koreatech.ac.kr");

        RestAssured
            .given()
            .param("auth_token", user.getAuthToken())
            .when()
            .get("/user/authenticate");

        User user1 = userRepository.getByEmail("koko123@koreatech.ac.kr");

        Assertions.assertThat(user1.isAuthed()).isTrue();
        verify(studentEventListener).onStudentRegister(any());
    }

    @Test
    @DisplayName("회원 가입 필수 파라미터를 안넣을시 400에러코드를 반환한다.")
    void studentRegisterBadRequest() {
        RestAssured
            .given()
            .body("""
                {
                  "major": "컴퓨터공학부",
                  "email": null,
                  "name": "김철수",
                  "password": "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268",
                  "nickname": "koko",
                  "gender": "0",
                  "is_graduated": false,
                  "student_number": "2021136012",
                  "phone_number": "010-0000-0000"
                }
                """)
            .contentType(ContentType.JSON)
            .when()
            .post("/user/student/register")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("한기대 이메일이 아닐시 400에러코드를 반환한다.")
    void studentRegisterInvalid() {
        RestAssured
            .given()
            .body("""
                {
                  "major": "컴퓨터공학부",
                  "email": "koko123@gmail.com",
                  "name": "김철수",
                  "password": "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268",
                  "nickname": "koko",
                  "gender": "0",
                  "is_graduated": false,
                  "student_number": "2021136012",
                  "phone_number": "010-0000-0000"
                }
                """)
            .contentType(ContentType.JSON)
            .when()
            .post("/user/student/register")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    @DisplayName("유효한 학번의 형식이 아닐시 400에러코드를 반환한다.")
    void studentRegisterStudentNumberInvalid() {
        RestAssured
            .given()
            .body("""
                {
                  "major": "컴퓨터공학부",
                  "email": "koko123@gmail.com",
                  "name": "김철수",
                  "password": "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268",
                  "nickname": "koko",
                  "gender": "0",
                  "is_graduated": false,
                  "student_number": "20211360123324231",
                  "phone_number": "010-0000-0000"
                }
                """)
            .contentType(ContentType.JSON)
            .when()
            .post("/user/student/register")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());

        RestAssured
            .given()
            .body("""
                {
                  "major": "컴퓨터공학부",
                  "email": "koko123@gmail.com",
                  "name": "김철수",
                  "password": "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268",
                  "nickname": "koko",
                  "gender": "0",
                  "is_graduated": false,
                  "student_number": "19911360123",
                  "phone_number": "010-0000-0000"
                }
                """)
            .contentType(ContentType.JSON)
            .when()
            .post("/user/student/register")
            .then()
            .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}
