package in.koreatech.koin.acceptance;

import static in.koreatech.koin.domain.user.model.UserIdentity.UNDERGRADUATE;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin.domain.coop.model.Coop;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.student.repository.StudentRepository;
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
    private UserRepository userRepository;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private UserFixture userFixture;

    @BeforeAll
    void setup() {
        clear();
    }

    @Test
    void 로그인을_진행한다_구_API_user_login() throws Exception {
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

    // 영양사 부분도 Coop 관련으로 넘길 예정
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

