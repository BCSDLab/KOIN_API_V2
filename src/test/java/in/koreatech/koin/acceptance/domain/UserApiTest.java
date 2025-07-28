package in.koreatech.koin.acceptance.domain;

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

import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.acceptance.AcceptanceTest;
import in.koreatech.koin.acceptance.fixture.DepartmentAcceptanceFixture;
import in.koreatech.koin.acceptance.fixture.UserAcceptanceFixture;
import in.koreatech.koin.domain.coop.model.Coop;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.verification.model.VerificationCode;
import in.koreatech.koin.domain.user.verification.repository.VerificationCodeRedisRepository;
import in.koreatech.koin.infrastructure.naver.service.NaverSmsService;

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
    private UserAcceptanceFixture userFixture;

    @Autowired
    private DepartmentAcceptanceFixture departmentFixture;

    @Autowired
    private VerificationCodeRedisRepository verificationCodeRedisRepository;

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
                    .header("User-Agent", userFixture.맥북userAgent헤더())
            )
            .andExpect(status().isCreated());
    }

    // 영양사로 넘길 예정
    @Test
    void 영양사가_로그인을_진행한다() throws Exception {
        Coop coop = userFixture.준기_영양사();
        String id = coop.getLoginId();
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
                    .header("User-Agent", userFixture.맥북userAgent헤더())
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
                    .header("User-Agent", userFixture.맥북userAgent헤더())
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
            .andExpect(status().isConflict());
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

        assertThat(userRepository.findByPhoneNumberAndUserTypeIn(phoneNumber, UserType.KOIN_USER_TYPES)).isNotPresent();
    }

    @Test
    void 전화번호_중복을_확인한다_중복이면_409() throws Exception {
        User user = userFixture.코인_유저();

        mockMvc.perform(
                get("/user/check/phone")
                    .param("phone", user.getPhoneNumber())
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isConflict());
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
            .andExpect(status().isConflict());
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
            .loginPw("1234")
            .nickname("주노")
            .name("최준호")
            .phoneNumber("010-1234-5678")
            .userType(STUDENT)
            .gender(UserGender.MAN)
            .email("test@koreatech.ac.kr")
            .loginId("test")
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
            .department(department)
            .userIdentity(UNDERGRADUATE)
            .isGraduated(false)
            .user(
                User.builder()
                    .name("최준호")
                    .nickname("주노")
                    .anonymousNickname("익명")
                    .phoneNumber("010-1234-5678")
                    .userType(STUDENT)
                    .gender(UserGender.MAN)
                    .email("test@koreatech.ac.kr")
                    .loginId("test")
                    .loginPw("1234")
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
                    .header("User-Agent", userFixture.맥북userAgent헤더())

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
                    .header("User-Agent", userFixture.맥북userAgent헤더())
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
        doNothing().when(naverSmsService).sendVerificationCode(any(), any());
        String phoneNumber = "01012345678";

        // when
        // SMS 인증번호 전송
        mockMvc.perform(
                post("/users/verification/sms/send")
                    .content("""
                        {
                          "phone_number": "%s"
                        }
                        """.formatted(phoneNumber))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        // Redis에서 인증번호 확인
        VerificationCode status = verificationCodeRedisRepository.findById(phoneNumber)
            .orElseThrow(() -> AuthorizationException.withDetail("verification: " + phoneNumber));
        String certificationCode = status.getVerificationCode();

        // then
        // SMS 인증번호 검증
        mockMvc.perform(
                post("/users/verification/sms/verify")
                    .content("""
                        {
                          "phone_number": "%s",
                          "verification_code": "%s"
                        }
                        """.formatted(phoneNumber, certificationCode))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());
    }

    @Test
    void 사용자가_SMS_인증번호_전송_후_잘못된_인증번호로_검증시_에러를_반환한다() throws Exception {
        // given
        doNothing().when(naverSmsService).sendVerificationCode(any(), any());
        String phoneNumber = "01012345678";

        // when
        // SMS 인증번호 전송
        mockMvc.perform(
                post("/users/verification/sms/send")
                    .content("""
                        {
                          "phone_number": "%s"
                        }
                        """.formatted(phoneNumber))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        // Redis에서 인증번호 확인
        VerificationCode status = verificationCodeRedisRepository.findById(phoneNumber)
            .orElseThrow(() -> AuthorizationException.withDetail("verification: " + phoneNumber));
        String certificationCode = status.getVerificationCode();
        String wrongCode = certificationCode.equals("123456") ? "654321" : "123456";

        // then
        // 잘못된 인증번호로 검증
        mockMvc.perform(
                post("/users/verification/sms/verify")
                    .content("""
                        {
                          "phone_number": "%s",
                          "verification_code": "%s"
                        }
                        """.formatted(phoneNumber, wrongCode))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    void 사용자가_SMS_인증번호_하루_5번_이상_발송시도시_429_에러를_반환한다() throws Exception {
        // given
        doNothing().when(naverSmsService).sendVerificationCode(any(), any());
        String phoneNumber = "01012345678";
        int maxDailyLimit = 5;

        // when
        // 5번까지 정상 발송
        for (int i = 0; i < maxDailyLimit; i++) {
            mockMvc.perform(
                    post("/users/verification/sms/send")
                        .content("""
                            {
                              "phone_number": "%s"
                            }
                            """.formatted(phoneNumber))
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
        }

        // then
        // 6번째 발송 시도시 400 반환
        mockMvc.perform(
                post("/users/verification/sms/send")
                    .content("""
                        {
                          "phone_number": "%s"
                        }
                        """.formatted(phoneNumber))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isTooManyRequests());
    }

    @Test
    void 사용자가_인증을_통해_ID를_찾는다() throws Exception {
        // given
        doNothing().when(naverSmsService).sendVerificationCode(any(), any());
        User user = userFixture.코인_유저();
        String phoneNumber = user.getPhoneNumber();

        // when
        // SMS 인증번호 전송
        mockMvc.perform(
                post("/users/verification/sms/send")
                    .content("""
                        {
                          "phone_number": "%s"
                        }
                        """.formatted(phoneNumber))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        // Redis에서 인증번호 확인
        VerificationCode status = verificationCodeRedisRepository.findById(phoneNumber)
            .orElseThrow(() -> AuthorizationException.withDetail("verification: " + phoneNumber));
        String certificationCode = status.getVerificationCode();

        // 인증번호 검증
        mockMvc.perform(
                post("/users/verification/sms/verify")
                    .content("""
                        {
                          "phone_number": "%s",
                          "verification_code": "%s"
                        }
                        """.formatted(phoneNumber, certificationCode))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        // ID 찾기
        mockMvc.perform(
                post("/users/id/find/sms")
                    .content("""
                        {
                          "phone_number": "%s"
                        }
                        """.formatted(phoneNumber))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.login_id").value(user.getLoginId()));
    }

    @Test
    void 사용자가_인증을_통해_비밀번호를_변경한다() throws Exception {
        // given
        doNothing().when(naverSmsService).sendVerificationCode(any(), any());
        User user = userFixture.코인_유저();
        String phoneNumber = user.getPhoneNumber();
        String newPassword = "12345";

        // when
        // SMS 인증번호 전송
        mockMvc.perform(
                post("/users/verification/sms/send")
                    .content("""
                        {
                          "phone_number": "%s"
                        }
                        """.formatted(phoneNumber))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        // Redis에서 인증번호 확인
        VerificationCode status = verificationCodeRedisRepository.findById(phoneNumber)
            .orElseThrow(() -> AuthorizationException.withDetail("verification: " + phoneNumber));
        String certificationCode = status.getVerificationCode();

        // 인증번호 검증
        mockMvc.perform(
                post("/users/verification/sms/verify")
                    .content("""
                        {
                          "phone_number": "%s",
                          "verification_code": "%s"
                        }
                        """.formatted(phoneNumber, certificationCode))
                    .contentType(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isOk());

        // 비밀번호 변경
        mockMvc.perform(
                post("/users/password/reset/sms")
                    .content("""
                        {
                          "login_id": "%s",
                          "phone_number": "%s",
                          "new_password": "%s"
                        }
                        """.formatted(user.getLoginId(), phoneNumber, newPassword))
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
                    .header("User-Agent", userFixture.맥북userAgent헤더())
            )
            .andExpect(status().isCreated());
    }
}
