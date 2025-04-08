package in.koreatech.koin.acceptance;

import static in.koreatech.koin.domain.user.model.UserIdentity.UNDERGRADUATE;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.AcceptanceTest;
import in.koreatech.koin._common.auth.JwtProvider;
import in.koreatech.koin.domain.coop.model.Coop;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.model.UserVerificationStatus;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.repository.UserVerificationStatusRedisRepository;
import in.koreatech.koin.fixture.DepartmentFixture;
import in.koreatech.koin.fixture.UserFixture;
import in.koreatech.koin.integration.naver.service.NaverSmsService;

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

    @Autowired
    private DepartmentFixture departmentFixture;

    @Autowired
    private UserVerificationStatusRedisRepository userVerificationStatusRedisRepository;

    // mock-up 인증 서버를 사용할 경우 슬랙 알림 발송 문제로 인해 sms 인증 컴포넌트 자체를 mocking 했습니다.
    @MockBean
    private NaverSmsService naverSmsService;

    @BeforeAll
    void setup() {
        clear();
    }

    @Test
    void 학생이_로그인을_진행한다_구_API_user_login() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();
        Student student = userFixture.성빈_학생(department);
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

    // 영양사로 넘길 예정
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
        Department department = departmentFixture.컴퓨터공학부();
        Student student = userFixture.성빈_학생(department);
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
        String email = "wrong account format";

        mockMvc.perform(
                get("/user/check/email")
                    .param("address", email)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 이메일이_중복인지_확인한다_중복이면_409() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();
        User user = userFixture.성빈_학생(department).getUser();

        mockMvc.perform(
                get("/user/check/email")
                    .param("address", user.getEmail())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("이미 존재하는 이메일입니다."));
    }

    @Test
    void 전화번호_중복을_확인한다() throws Exception {
        String phoneNumber = "01012345678";

        mockMvc.perform(
                get("/user/check/phone")
                    .param("phone", phoneNumber)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        assertThat(userRepository.findByPhoneNumber(phoneNumber)).isNotPresent();
    }

    @Test
    void 전화번호_중복을_확인한다_중복이면_409() throws Exception {
        User user = userFixture.코인_유저();

        mockMvc.perform(
                get("/user/check/phone")
                    .param("phone", user.getPhoneNumber())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.message").value("이미 존재하는 전화번호입니다."));
    }

    @Test
    void 닉네임_중복일때_상태코드_409를_반환한다() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();
        User user = userFixture.성빈_학생(department).getUser();

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
        Department department = departmentFixture.컴퓨터공학부();
        User user = userFixture.성빈_학생(department).getUser();

        mockMvc.perform(
                get("/user/check/nickname")
                    .param("nickname", "철수")
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
    }

    @Test
    void 사용자가_비밀번호를_통해_자신이_맞는지_인증한다_비밀번호가_다르면_400_반환() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();
        Student student = userFixture.준호_학생(department, null);
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
    void 닉네임_제약조건_위반시_상태코드_400를_반환한다() throws Exception {
        User user = User.builder()
            .password("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .userId("test")
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
        Department department = departmentFixture.컴퓨터공학부();
        Student student = Student.builder()
            .studentNumber("2019136135")
            .anonymousNickname("익명")
            .department(department)
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
                    .userId("test")
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
    void 사용자가_비밀번호를_변경하고_기존_비밀번호로_로그인하면_에러를_반환한다() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();
        Student student = userFixture.준호_학생(department, null);
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
                          "account": "%s",
                          "password": "%s"
                        }
                        """.formatted(student.getUser().getEmail(), "1234"))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 사용자가_로그인상태인지_확인한다() throws Exception {
        Department department = departmentFixture.컴퓨터공학부();
        Student student = userFixture.준호_학생(department, null);
        String accessToken = userFixture.getToken(student.getUser());

        mockMvc.perform(
                get("/user/check/login")
                    .param("access_token", accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
    }

    @Test
    void 사용자가_SMS_인증번호_전송_및_검증한다() throws Exception {
        // given
        // SMS 서비스 모킹 설정
        doNothing().when(naverSmsService).sendVerificationCode(any(), any());
        String phoneNumber = "01012345678";

        // when - SMS 인증번호 전송
        mockMvc.perform(
                post("/user/verification/send")
                    .content("""
                        {
                          "target": "%s"
                        }
                        """.formatted(phoneNumber))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        // Redis에서 인증번호 확인
        UserVerificationStatus status = userVerificationStatusRedisRepository.getById(phoneNumber);
        String certificationCode = status.getVerificationCode();

        // then - SMS 인증번호 검증
        mockMvc.perform(
                post("/user/verification/verify")
                    .content("""
                        {
                          "target": "%s",
                          "code": "%s"
                        }
                        """.formatted(phoneNumber, certificationCode))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
    }

    @Test
    void 사용자가_SMS_인증번호_전송_후_잘못된_인증번호로_검증시_에러를_반환한다() throws Exception {
        // given
        // SMS 서비스 모킹 설정
        doNothing().when(naverSmsService).sendVerificationCode(any(), any());
        String phoneNumber = "01012345678";

        // when - SMS 인증번호 전송
        mockMvc.perform(
                post("/user/verification/send")
                    .content("""
                        {
                          "target": "%s"
                        }
                        """.formatted(phoneNumber))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        // Redis에서 인증번호 확인
        UserVerificationStatus status = userVerificationStatusRedisRepository.getById(phoneNumber);
        String certificationCode = status.getVerificationCode();
        String wrongCode = certificationCode.equals("123456") ? "654321" : "123456";

        // then - 잘못된 인증번호로 검증
        mockMvc.perform(
                post("/user/verification/verify")
                    .content("""
                        {
                          "target": "%s",
                          "code": "%s"
                        }
                        """.formatted(phoneNumber, wrongCode))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 사용자가_SMS_인증번호_하루_5번_이상_발송시도시_에러를_반환한다() throws Exception {
        // given
        // SMS 서비스 모킹 설정
        doNothing().when(naverSmsService).sendVerificationCode(any(), any());
        String phoneNumber = "01012345678";
        int maxDailyLimit = 5;

        // when - 5번까지 정상 발송
        for (int i = 0; i < maxDailyLimit; i++) {
            mockMvc.perform(
                    post("/user/verification/send")
                        .content("""
                            {
                              "target": "%s"
                            }
                            """.formatted(phoneNumber))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
        }

        // then - 6번째 발송 시도시 400 반환
        mockMvc.perform(
                post("/user/verification/send")
                    .content("""
                        {
                          "target": "%s"
                        }
                        """.formatted(phoneNumber))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 사용자가_인증을_통해_ID를_찾는다() throws Exception {
        // given
        // SMS 서비스 모킹 설정
        doNothing().when(naverSmsService).sendVerificationCode(any(), any());
        String phoneNumber = "01012345678";

        // 사용자 생성
        User user = userFixture.코인_유저();
        user.update(user.getNickname(), user.getName(), phoneNumber, user.getGender());
        userRepository.save(user);

        // when - SMS 인증번호 전송
        mockMvc.perform(
                post("/user/verification/send")
                    .content("""
                        {
                          "target": "%s"
                        }
                        """.formatted(phoneNumber))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        // Redis에서 인증번호 확인
        UserVerificationStatus status = userVerificationStatusRedisRepository.getById(phoneNumber);
        String certificationCode = status.getVerificationCode();

        // 인증번호 검증
        mockMvc.perform(
                post("/user/verification/verify")
                    .content("""
                        {
                          "target": "%s",
                          "code": "%s"
                        }
                        """.formatted(phoneNumber, certificationCode))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        // ID 찾기
        mockMvc.perform(
                post("/user/id/find")
                    .content("""
                        {
                          "target": "%s"
                        }
                        """.formatted(phoneNumber))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.user_id").value(user.getUserId()));
    }

    @Test
    void 사용자가_인증을_통해_비밀번호를_변경한다() throws Exception {
        // given
        // SMS 서비스 모킹 설정
        doNothing().when(naverSmsService).sendVerificationCode(any(), any());
        String phoneNumber = "01012345678";
        String newPassword = "71848878b759cce064131e4b717ee07cd24b88e1ac8ba17c5ca317674eca25b7";

        // 사용자 생성
        User user = userFixture.코인_유저();
        user.update(user.getNickname(), user.getName(), phoneNumber, user.getGender());
        userRepository.save(user);

        // when - SMS 인증번호 전송
        mockMvc.perform(
                post("/user/verification/send")
                    .content("""
                        {
                          "target": "%s"
                        }
                        """.formatted(phoneNumber))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        // Redis에서 인증번호 확인
        UserVerificationStatus status = userVerificationStatusRedisRepository.getById(phoneNumber);
        String certificationCode = status.getVerificationCode();

        // 인증번호 검증
        mockMvc.perform(
                post("/user/verification/verify")
                    .content("""
                        {
                          "target": "%s",
                          "code": "%s"
                        }
                        """.formatted(phoneNumber, certificationCode))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        // 비밀번호 변경
        mockMvc.perform(
                post("/user/password/reset")
                    .content("""
                        {
                          "target": "%s",
                          "password": "%s"
                        }
                        """.formatted(phoneNumber, newPassword))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        // 변경된 비밀번호로 로그인 확인
        mockMvc.perform(
                post("/user/login")
                    .content("""
                        {
                          "email": "%s",
                          "password": "%s"
                        }
                        """.formatted(user.getEmail(), newPassword))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isCreated());
    }
}
