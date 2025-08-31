package in.koreatech.koin.acceptance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.support.TransactionTemplate;

import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.MajorAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.dept.model.Dept;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.model.redis.UnAuthenticatedStudentInfo;
import in.koreatech.koin.domain.student.repository.StudentRedisRepository;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.JwtProvider;

public class StudentApiTest extends AcceptanceTest {
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
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    @Autowired
    private MajorAcceptanceFixture majorFixture;

    @BeforeAll
    void setup() {
        clear();
    }

    @Test
    void 학생이_로그인을_진행한다_신규_API_student_login() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();

        Student student = userFixture.성빈_학생(department);
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
                    .header("User-Agent", userFixture.맥북userAgent헤더())
            )
            .andExpect(status().isCreated());
    }

    @Test
    void 올바른_학생계정인지_확인한다() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();

        Student student = userFixture.준호_학생(department, null);
        String token = userFixture.getToken(student.getUser());

        mockMvc.perform(
                get("/user/student/me")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "anonymous_nickname": "익명_주노",
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
        Department department = departmentFixture.컴퓨터공학부();

        userFixture.준호_학생(department, null);
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
        Department department = departmentFixture.컴퓨터공학부();

        Student student = userFixture.준호_학생(department, null);
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
        majorFixture.기계공학전공(departmentFixture.기계공학부());
        Department department = departmentFixture.컴퓨터공학부();

        Student student = userFixture.준호_학생(department, null);
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
                        "anonymous_nickname": "익명_주노",
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
        Department department = departmentFixture.컴퓨터공학부();

        Student student = userFixture.준호_학생(department, null);
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
        Department department = departmentFixture.컴퓨터공학부();

        Student student = userFixture.준호_학생(department, null);
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
        Department department = departmentFixture.컴퓨터공학부();

        userFixture.준호_학생(department, null);
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
        Department department = departmentFixture.컴퓨터공학부();
        Major major = majorFixture.컴퓨터공학전공(department);

        Student student = userFixture.준호_학생(department, major);
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
        Department department = departmentFixture.컴퓨터공학부();

        Student 준호 = userFixture.준호_학생(department, null);
        Student 성빈 = userFixture.성빈_학생(department);
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

        Optional<UnAuthenticatedStudentInfo> student = studentRedisRepository.findById("koko123@koreatech.ac.kr");

        assertSoftly(
            softly -> {
                softly.assertThat(student).isNotNull();
                softly.assertThat(student.get().getNickname()).isEqualTo("koko");
                softly.assertThat(student.get().getName()).isEqualTo("김철수");
                softly.assertThat(student.get().getPhoneNumber()).isEqualTo("01000000000");
                softly.assertThat(student.get().getEmail()).isEqualTo("koko123@koreatech.ac.kr");
                softly.assertThat(student.get().getStudentNumber()).isEqualTo("2021136012");
                softly.assertThat(student.get().getDepartment()).isEqualTo(Dept.COMPUTER_SCIENCE.getName());
                forceVerify(() -> verify(studentEventListener).onStudentRegisterRequestEvent(any()));
                setup();
            }
        );
    }

    @Test
    void 이메일_요청을_확인_후_회원가입_이벤트가_발생하고_Redis에_저장된_정보가_삭제된다() throws Exception {
        majorFixture.컴퓨터공학전공(departmentFixture.컴퓨터공학부());
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

        Optional<UnAuthenticatedStudentInfo> student = studentRedisRepository.findById("koko123@koreatech.ac.kr");
        mockMvc.perform(
                get("/user/authenticate")
                    .queryParam("auth_token", student.get().getAuthToken())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andReturn();

        User user = userRepository.getByEmailAndUserTypeIn("koko123@koreatech.ac.kr", UserType.KOIN_USER_TYPES);
        assertThat(studentRedisRepository.findById("koko123@koreatech.ac.kr")).isEmpty();

        assertThat(user.isAuthed()).isTrue();
        forceVerify(() -> verify(studentEventListener).onStudentRegisterEvent(any()));
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
}
