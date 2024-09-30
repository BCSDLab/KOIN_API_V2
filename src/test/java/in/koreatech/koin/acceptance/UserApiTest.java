package in.koreatech.koin.acceptance;

import static in.koreatech.koin.domain.user.model.UserIdentity.UNDERGRADUATE;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Optional;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.coop.model.Coop;
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

@SuppressWarnings("NonAsciiCharacters")
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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

    @BeforeAll
    void setup() {
        clear();
    }

    @Test
    void 학생이_로그인을_진행한다_구_API_user_login() throws Exception {
        Student student = userFixture.성빈_학생();
        String email = student.getUser().getEmail();
        String password = "1234";

        mockMvc.perform(
                post("/user/login")
                    .content("""
                        {
                          "email" : "%s",
                          "password" : "%s"
                        }
                        """.formatted(email, password))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated());
    }

    @Test
    void 학생이_로그인을_진행한다_신규_API_student_login() throws Exception {
        Student student = userFixture.성빈_학생();
        String email = student.getUser().getEmail();
        String password = "1234";

        mockMvc.perform(
                post("/student/login")
                    .content("""
                        {
                          "email" : "%s",
                          "password" : "%s"
                        }
                        """.formatted(email, password))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated());
    }

    @Test
    void 영양사가_로그인을_진행한다() throws Exception {
        Coop coop = userFixture.준기_영양사();
        String id = coop.getCoopId();
        String password = "1234";

        mockMvc.perform(
                post("/coop/login")
                    .content("""
                        {
                          "id" : "%s",
                          "password" : "%s"
                        }
                        """.formatted(id, password))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated());
    }

    @Test
    void 올바른_영양사_계정인지_확인한다() throws Exception {
        Coop coop = userFixture.준기_영양사();
        String token = userFixture.getToken(coop.getUser());

        mockMvc.perform(
                get("/user/coop/me")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
    }

    @Test
    void 올바른_학생계정인지_확인한다() throws Exception {
        Student student = userFixture.준호_학생();
        String token = userFixture.getToken(student.getUser());

        mockMvc.perform(
                get("/user/student/me")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "anonymous_nickname": "익명",
                    "email": "juno@koreatech.ac.kr",
                    "gender": 0,
                    "major": "컴퓨터공학부",
                    "name": "테스트용_준호",
                    "nickname": "준호",
                    "phone_number": "01012345678",
                    "student_number": "2019136135"
                }
                """));
    }

    @Test
    void 올바른_학생계정인지_확인한다_토큰_정보가_올바르지_않으면_401() throws Exception {
        userFixture.준호_학생();
        String token = "invalidToken";

        mockMvc.perform(
                get("/user/student/me")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 올바른_학생계정인지_확인한다_회원을_찾을_수_없으면_404() throws Exception {
        Student student = userFixture.준호_학생();
        String token = jwtProvider.createToken(student.getUser());
        transactionTemplate.executeWithoutResult(status ->
            studentRepository.deleteByUserId(student.getId())
        );

        mockMvc.perform(
                get("/user/student/me")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNotFound());
    }

    @Test
    void 학생이_정보를_수정한다() throws Exception {
        Student student = userFixture.준호_학생();
        String token = userFixture.getToken(student.getUser());

        mockMvc.perform(
                put("/user/student/me")
                    .header("Authorization", "Bearer " + token)
                    .content("""
                        {
                          "gender" : 1,
                          "major" : "기계공학부",
                          "name" : "서정빈",
                          "password" : "0c4be6acaba1839d3433c1ccf04e1eec4d1fa841ee37cb019addc269e8bc1b77",
                          "nickname" : "duehee",
                          "phone_number" : "01023456789",
                          "student_number" : "2019136136"
                        }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                    {
                        "anonymous_nickname": "익명",
                        "email": "juno@koreatech.ac.kr",
                        "gender": 1,
                        "major": "기계공학부",
                        "name": "서정빈",
                        "nickname": "duehee",
                        "phone_number": "01023456789",
                        "student_number": "2019136136"
                    }
                """));

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
    }

    @Test
    void 학생이_정보를_수정한다_학번의_형식이_맞지_않으면_400() throws Exception {
        Student student = userFixture.준호_학생();
        String token = userFixture.getToken(student.getUser());

        mockMvc.perform(
                put("/user/student/me")
                    .header("Authorization", "Bearer " + token)
                    .content("""
                          {
                            "gender" : 0,
                            "major" : "메카트로닉스공학부",
                            "name" : "최주노",
                            "nickname" : "juno",
                            "phone_number" : "01023456789",
                            "student_number" : "201913613"
                          }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 학생이_정보를_수정한다_학부의_형식이_맞지_않으면_400() throws Exception {
        Student student = userFixture.준호_학생();
        String token = userFixture.getToken(student.getUser());

        mockMvc.perform(
                put("/user/student/me")
                    .header("Authorization", "Bearer " + token)
                    .content("""
                          {
                            "gender" : 0,
                            "major" : "경영학과",
                            "name" : "최주노",
                            "nickname" : "juno",
                            "phone_number" : "01023456789",
                            "student_number" : "2019136136"
                          }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 학생이_정보를_수정한다_토큰이_올바르지_않다면_401() throws Exception {
        userFixture.준호_학생();
        String token = "invalidToken";

        mockMvc.perform(
                put("/user/student/me")
                    .header("Authorization", "Bearer " + token)
                    .content("""
                          {
                            "gender" : 0,
                            "major" : "메카트로닉스공학부",
                            "name" : "최주노",
                            "nickname" : "juno",
                            "phone_number" : "01023456789",
                            "student_number" : "2019136136"
                          }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isUnauthorized());
    }

    @Test
    void 학생이_정보를_수정한다_회원을_찾을_수_없다면_404() throws Exception {
        Student student = userFixture.준호_학생();
        String token = userFixture.getToken(student.getUser());
        transactionTemplate.executeWithoutResult(status ->
            studentRepository.deleteByUserId(student.getId())
        );

        mockMvc.perform(
                put("/user/student/me")
                    .header("Authorization", "Bearer " + token)
                    .content("""
                          {
                            "gender" : 0,
                            "major" : "메카트로닉스공학부",
                            "name" : "최주노",
                            "nickname" : "juno",
                            "phone_number" : "01023456789",
                            "student_number" : "2019136136"
                          }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNotFound());
    }

    @Test
    void 학생이_정보를_수정한다_이미_있는_닉네임이라면_409() throws Exception {
        Student 준호 = userFixture.준호_학생();
        Student 성빈 = userFixture.성빈_학생();
        String token = userFixture.getToken(준호.getUser());

        mockMvc.perform(
                put("/user/student/me")
                    .header("Authorization", "Bearer " + token)
                    .content(String.format("""
                         {
                            "gender" : 0,
                            "major" : "테스트학과",
                            "name" : "최주노",
                            "nickname" : "%s",
                            "phone_number" : "01023456789",
                            "student_number" : "2019136136"
                         }
                        """, 성빈.getUser().getNickname()))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isConflict());
    }

    @Test
    void 회원이_탈퇴한다() throws Exception {
        Student student = userFixture.성빈_학생();
        String token = userFixture.getToken(student.getUser());

        mockMvc.perform(
                delete("/user")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNoContent());

        assertThat(userRepository.findById(student.getId())).isNotPresent();
    }

    @Test
    void 이메일이_중복인지_확인한다() throws Exception {
        String email = "notduplicated@koreatech.ac.kr";

        mockMvc.perform(
                get("/user/check/email")
                    .param("address", email)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        assertThat(userRepository.findByEmail(email)).isNotPresent();
    }

    @Test
    void 이메일이_중복인지_확인한다_파라미터에_이메일을_포함하지_않으면_400() throws Exception {
        mockMvc.perform(
                get("/user/check/email")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 이메일이_중복인지_확인한다_잘못된_이메일_형식이면_400() throws Exception {
        String email = "wrong email format";

        mockMvc.perform(
                get("/user/check/email")
                    .param("address", email)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 이메일이_중복인지_확인한다_중복이면_422() throws Exception {
        User user = userFixture.성빈_학생().getUser();

        mockMvc.perform(
                get("/user/check/email")
                    .param("address", user.getEmail())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("존재하는 이메일입니다."));
    }

    @Test
    void 닉네임_중복일때_상태코드_409를_반환한다() throws Exception {
        User user = userFixture.성빈_학생().getUser();

        mockMvc.perform(
                get("/user/check/nickname")
                    .param("nickname", user.getNickname())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("이미 존재하는 닉네임입니다."));
    }

    @Test
    void 닉네임_중복이_아닐시_상태코드_200을_반환한다() throws Exception {
        User user = userFixture.성빈_학생().getUser();

        mockMvc.perform(
                get("/user/check/nickname")
                    .param("nickname", "철수")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
    }

    @Test
    void 닉네임_제약조건_위반시_상태코드_400를_반환한다() throws Exception {
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

        mockMvc.perform(
                get("/user/check/nickname")
                    .param("nickname", "철".repeat(11))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());

        mockMvc.perform(
                get("/user/check/nickname")
                    .param("nickname", "")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 로그인된_사용자의_권한을_조회한다() throws Exception {
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
        User user = student.getUser();

        mockMvc.perform(
                get("/user/auth")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user_type").value(user.getUserType().getValue()));
    }

    @Test
    void 학생_회원가입_후_학교_이메일요청_이벤트가_발생하고_Redis에_저장된다() throws Exception {
        mockMvc.perform(
                post("/user/student/register")
                    .content("""
                        {
                          "major": "컴퓨터공학부",
                          "email": "koko123@koreatech.ac.kr",
                          "name": "김철수",
                          "password": "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268",
                          "nickname": "koko",
                          "gender": "0",
                          "is_graduated": false,
                          "student_number": "2021136012",
                          "phone_number": "01000000000"
                        }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        Optional<StudentTemporaryStatus> student = studentRedisRepository.findById("koko123@koreatech.ac.kr");

        assertSoftly(
            softly -> {
                softly.assertThat(student).isNotNull();
                softly.assertThat(student.get().getNickname()).isEqualTo("koko");
                softly.assertThat(student.get().getName()).isEqualTo("김철수");
                softly.assertThat(student.get().getPhoneNumber()).isEqualTo("01000000000");
                softly.assertThat(student.get().getEmail()).isEqualTo("koko123@koreatech.ac.kr");
                softly.assertThat(student.get().getStudentNumber()).isEqualTo("2021136012");
                softly.assertThat(student.get().getDepartment()).isEqualTo(Dept.COMPUTER_SCIENCE.getName());
                forceVerify(() -> verify(studentEventListener).onStudentEmailRequest(any()));
                clear();
                setup();
            }
        );
    }

    @Test
    void 이메일_요청을_확인_후_회원가입_이벤트가_발생하고_Redis에_저장된_정보가_삭제된다() throws Exception {
        mockMvc.perform(
                post("/user/student/register")
                    .content("""
                        {
                          "major": "컴퓨터공학부",
                          "email": "koko123@koreatech.ac.kr",
                          "name": "김철수",
                          "password": "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268",
                          "nickname": "koko",
                          "gender": "0",
                          "is_graduated": false,
                          "student_number": "2021136012",
                          "phone_number": "01000000000"
                        }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        Optional<StudentTemporaryStatus> student = studentRedisRepository.findById("koko123@koreatech.ac.kr");
        mockMvc.perform(
                get("/user/authenticate")
                    .queryParam("auth_token", student.get().getAuthToken())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andReturn();

        User user = userRepository.getByEmail("koko123@koreatech.ac.kr");
        assertThat(studentRedisRepository.findById("koko123@koreatech.ac.kr")).isEmpty();

        assertThat(user.isAuthed()).isTrue();
        forceVerify(() -> verify(studentEventListener).onStudentRegister(any()));
        clear();
        setup();
    }

    @Test
    void 회원_가입_필수_파라미터를_안넣을시_400에러코드를_반환한다() throws Exception {
        mockMvc.perform(
                post("/user/student/register")
                    .content("""
                        {
                          "major": "컴퓨터공학부",
                          "email": null,
                          "name": "김철수",
                          "password": "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268",
                          "nickname": "koko",
                          "gender": "0",
                          "is_graduated": false,
                          "student_number": "2021136012",
                          "phone_number": "01000000000"
                        }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 한기대_이메일이_아닐시_400에러코드를_반환한다() throws Exception {
        mockMvc.perform(
                post("/user/student/register")
                    .content("""
                        {
                          "major": "컴퓨터공학부",
                          "email": "koko123@gmail.com",
                          "name": "김철수",
                          "password": "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268",
                          "nickname": "koko",
                          "gender": "0",
                          "is_graduated": false,
                          "student_number": "2021136012",
                          "phone_number": "01000000000"
                        }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 유효한_학번의_형식이_아닐시_400에러코드를_반환한다() throws Exception {
        mockMvc.perform(
                post("/user/student/register")
                    .content("""
                        {
                          "major": "컴퓨터공학부",
                          "email": "koko123@gmail.com",
                          "name": "김철수",
                          "password": "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268",
                          "nickname": "koko",
                          "gender": "0",
                          "is_graduated": false,
                          "student_number": "20211360123324231",
                          "phone_number": "01000000000"
                        }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());

        mockMvc.perform(
                post("/user/student/register")
                    .content("""
                        {
                          "major": "컴퓨터공학부",
                          "email": "koko123@gmail.com",
                          "name": "김철수",
                          "password": "cd06f8c2b0dd065faf6ef910c7f15934363df71c33740fd245590665286ed268",
                          "nickname": "koko",
                          "gender": "0",
                          "is_graduated": false,
                          "student_number": "19911360123",
                          "phone_number": "01000000000"
                        }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 사용자가_비밀번호를_통해_자신이_맞는지_인증한다() throws Exception {
        Student student = userFixture.준호_학생();
        String token = userFixture.getToken(student.getUser());

        mockMvc.perform(
                post("/user/check/password")
                    .header("Authorization", "Bearer " + token)
                    .content("""
                          {
                            "password": "1234"
                          }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
    }

    @Test
    void 사용자가_비밀번호를_통해_자신이_맞는지_인증한다_비밀번호가_다르면_400_반환() throws Exception {
        Student student = userFixture.준호_학생();
        String token = userFixture.getToken(student.getUser());

        mockMvc.perform(
                post("/user/check/password")
                    .header("Authorization", "Bearer " + token)
                    .content("""
                          {
                            "password": "123"
                          }
                        """)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 사용자가_비밀번호를_변경하고_기존_비밀번호로_로그인하면_에러를_반환한다() throws Exception {
        Student student = userFixture.준호_학생();
        String accessToken = userFixture.getToken(student.getUser());
        String newPassword = "newPassword1234";

        mockMvc.perform(
                put("/user/change/password")
                    .header("Authorization", "Bearer " + accessToken)
                    .content("""
                        {
                          "password": "%s"
                        }
                        """.formatted(newPassword))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        mockMvc.perform(
                post("/user/login")
                    .content("""
                        {
                          "email": "%s",
                          "password": "%s"
                        }
                        """.formatted(student.getUser().getEmail(), newPassword))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated());

        mockMvc.perform(
                post("/user/login")
                    .content("""
                        {
                          "email": "%s",
                          "password": "%s"
                        }
                        """.formatted(student.getUser().getEmail(), "1234"))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 사용자가_로그인상태인지_확인한다() throws Exception {
        Student student = userFixture.준호_학생();
        String accessToken = userFixture.getToken(student.getUser());

        mockMvc.perform(
                get("/user/check/login")
                    .param("access_token", accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
    }
}
