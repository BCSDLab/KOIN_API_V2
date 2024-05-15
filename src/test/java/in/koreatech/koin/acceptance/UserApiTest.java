package in.koreatech.koin.acceptance;

import static in.koreatech.koin.domain.user.model.UserIdentity.UNDERGRADUATE;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.assertj.core.api.SoftAssertions;
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
import in.koreatech.koin.domain.user.model.redis.StudentTemporaryStatus;
import in.koreatech.koin.domain.user.repository.StudentRedisRepository;
import in.koreatech.koin.domain.user.repository.StudentRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.support.JsonAssertions;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;

@SuppressWarnings("NonAsciiCharacters")
class UserApiTest extends AcceptanceTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentRedisRepository studentRedisRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private UserFixture userFixture;

    @Test
    @DisplayName("올바른 영양사 계정인지 확인한다")
    void coopCheckMe() {
        User user = userFixture.준기_영양사();
        String token = userFixture.getToken(user);

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/user/coop/me")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();
    }

    @Test
    @DisplayName("올바른 학생계정인지 확인한다")
    void studentCheckMe() {
        Student student = userFixture.준호_학생();
        String token = userFixture.getToken(student.getUser());

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .get("/user/student/me")
            .then()
            .statusCode(HttpStatus.OK.value())
            .extract();

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "anonymous_nickname": "익명",
                    "email": "juno@koreatech.ac.kr",
                    "gender": 0,
                    "major": "컴퓨터공학부",
                    "name": "테스트용_준호",
                    "nickname": "준호",
                    "phone_number": "010-1234-5678",
                    "student_number": "2019136135"
                }
                """);
    }

    @Test
    @DisplayName("올바른 학생계정인지 확인한다 - 토큰 정보가 올바르지 않으면  401")
    void studentCheckMeUnAuthorized() {
        userFixture.준호_학생();
        String token = "invalidToken";

        var response = RestAssured
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
        Student student = userFixture.준호_학생();
        String token = jwtProvider.createToken(student.getUser());
        transactionTemplate.executeWithoutResult(status ->
            studentRepository.deleteByUserId(student.getId())
        );

        RestAssured
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
        Student student = userFixture.준호_학생();
        String token = userFixture.getToken(student.getUser());

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
                  {
                    "gender" : 1,
                    "major" : "기계공학부",
                    "name" : "서정빈",
                    "password" : "0c4be6acaba1839d3433c1ccf04e1eec4d1fa841ee37cb019addc269e8bc1b77",
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

        transactionTemplate.executeWithoutResult(status -> {
            Student result = studentRepository.getById(student.getId());
            SoftAssertions.assertSoftly(
                softly -> {
                    softly.assertThat(result.getUser().getName()).isEqualTo("서정빈");
                    softly.assertThat(result.getUser().getNickname()).isEqualTo("duehee");
                    softly.assertThat(result.getUser().getName()).isEqualTo("서정빈");
                    softly.assertThat(result.getUser().getGender()).isEqualTo(UserGender.from(1));
                    softly.assertThat(result.getStudentNumber()).isEqualTo("2019136136");
                }
            );
        });

        JsonAssertions.assertThat(response.asPrettyString())
            .isEqualTo("""
                {
                    "anonymous_nickname": "익명",
                    "email": "juno@koreatech.ac.kr",
                    "gender": 1,
                    "major": "기계공학부",
                    "name": "서정빈",
                    "nickname": "duehee",
                    "phone_number": "010-2345-6789",
                    "student_number": "2019136136"
                }
                """);
    }

    @Test
    @DisplayName("학생이 정보를 수정한다 - 학번의 형식이 맞지 않으면 400")
    void studentUpdateMeNotValidStudentNumber() {
        Student student = userFixture.준호_학생();
        String token = userFixture.getToken(student.getUser());

        RestAssured
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
        Student student = userFixture.준호_학생();
        String token = userFixture.getToken(student.getUser());

        RestAssured
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
        userFixture.준호_학생();
        String token = "invalidToken";

        RestAssured
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
        Student student = userFixture.준호_학생();
        String token = userFixture.getToken(student.getUser());
        transactionTemplate.executeWithoutResult(status ->
            studentRepository.deleteByUserId(student.getId())
        );

        RestAssured
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
        Student 준호 = userFixture.준호_학생();
        Student 성빈 = userFixture.성빈_학생();
        String token = userFixture.getToken(준호.getUser());

        var response = RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body(String.format("""
                 {
                    "gender" : 0,
                    "major" : "테스트학과",
                    "name" : "최주노",
                    "nickname" : "%s",
                    "phone_number" : "010-2345-6789",
                    "student_number" : "2019136136"
                 }
                """, 성빈.getUser().getNickname()))
            .when()
            .put("/user/student/me")
            .then()
            .statusCode(HttpStatus.CONFLICT.value())
            .extract();
    }

    @Test
    @DisplayName("회원이 탈퇴한다")
    void userWithdraw() {
        Student student = userFixture.성빈_학생();
        String token = userFixture.getToken(student.getUser());

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .when()
            .delete("/user")
            .then()
            .statusCode(HttpStatus.NO_CONTENT.value())
            .extract();

        assertThat(userRepository.findById(student.getId())).isNotPresent();
    }

    @Test
    @DisplayName("이메일이 중복인지 확인한다")
    void emailCheckExists() {
        String email = "notduplicated@koreatech.ac.kr";

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
    @DisplayName("이메일이 중복인지 확인한다 - 파라미터에 이메일을 포함하지 않으면 400")
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
        User user = userFixture.성빈_학생().getUser();

        var response = RestAssured
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
        User user = userFixture.성빈_학생().getUser();

        var response = RestAssured
            .given()
            .when()
            .param("nickname", user.getNickname())
            .get("/user/check/nickname")
            .then()
            .statusCode(HttpStatus.CONFLICT.value())
            .extract();

        assertThat(response.body().jsonPath().getString("message"))
            .contains("이미 존재하는 닉네임입니다.");
    }

    @Test
    @DisplayName("닉네임 중복이 아닐시 상태코드 200을 반환한다.")
    void checkDuplicationOfNickname() {
        User user = userFixture.성빈_학생().getUser();

        RestAssured
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
            .userIdentity(UNDERGRADUATE)
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

        var response = RestAssured
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
                softly.assertThat(response.body().jsonPath().getString("user_type"))
                    .isEqualTo(user.getUserType().getValue());
            }
        );
    }

    @Test
    @DisplayName("학생 회원가입 후 학교 이메일요청 이벤트가 발생한다.")
    void studentRegister() {
        var response = RestAssured
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
            .statusCode(HttpStatus.OK.value())
            .extract();

        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Optional<StudentTemporaryStatus> student = studentRedisRepository.findById("koko123@koreatech.ac.kr");

                assertSoftly(
                    softly -> {
                        softly.assertThat(student).isNotNull();
                        softly.assertThat(student.get().getNickname()).isEqualTo("koko");
                        softly.assertThat(student.get().getName()).isEqualTo("김철수");
                        softly.assertThat(student.get().getPhoneNumber()).isEqualTo("010-0000-0000");
                        softly.assertThat(student.get().getKey()).isEqualTo("koko123@koreatech.ac.kr");
                        softly.assertThat(student.get().getStudentNumber()).isEqualTo("2021136012");
                        softly.assertThat(student.get().getDepartment()).isEqualTo(Dept.COMPUTER_SCIENCE.getName());
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
            .statusCode(HttpStatus.OK.value())
            .extract();

        Optional<StudentTemporaryStatus> student = studentRedisRepository.findById("koko123@koreatech.ac.kr");

        RestAssured
            .given()
            .param("auth_token", student.get().getAuthToken())
            .when()
            .get("/user/authenticate")
            .then();

        User user = userRepository.getByEmail("koko123@koreatech.ac.kr");

        assertThat(user.isAuthed()).isTrue();
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

    @Test
    @DisplayName("사용자가 비밀번호를 통해 자신이 맞는지 인증한다.")
    void userCheckPassword() {
        Student student = userFixture.준호_학생();
        String token = userFixture.getToken(student.getUser());

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
                  {
                    "password": "1234"
                  }
                """)
            .when()
            .post("/user/check/password")
            .then()
            .statusCode(HttpStatus.OK.value());
    }

    @Test
    @DisplayName("사용자가 비밀번호를 통해 자신이 맞는지 인증한다. - 비밀번호가 다르면 401 반환")
    void userCheckPasswordInvalid() {
        Student student = userFixture.준호_학생();
        String token = userFixture.getToken(student.getUser());

        RestAssured
            .given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("""
                  {
                    "password": "1233"
                  }
                """)
            .when()
            .post("/user/check/password")
            .then()
            .statusCode(HttpStatus.UNAUTHORIZED.value());
    }
}
