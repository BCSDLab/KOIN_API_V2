package in.koreatech.koin.unit.domain.student.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.ModelAndView;

import in.koreatech.koin.admin.abtest.useragent.UserAgentInfo;
import in.koreatech.koin.common.event.StudentFindPasswordEvent;
import in.koreatech.koin.common.event.StudentRegisterEvent;
import in.koreatech.koin.common.event.StudentRegisterRequestEvent;
import in.koreatech.koin.common.event.UserMarketingAgreementEvent;
import in.koreatech.koin.domain.graduation.service.GraduationService;
import in.koreatech.koin.domain.student.dto.RegisterStudentRequest;
import in.koreatech.koin.domain.student.dto.RegisterStudentRequestV2;
import in.koreatech.koin.domain.student.dto.StudentLoginRequest;
import in.koreatech.koin.domain.student.dto.StudentLoginResponse;
import in.koreatech.koin.domain.student.dto.StudentResponse;
import in.koreatech.koin.domain.student.dto.StudentWithAcademicResponse;
import in.koreatech.koin.domain.student.dto.UpdateStudentRequest;
import in.koreatech.koin.domain.student.dto.UpdateStudentRequestV2;
import in.koreatech.koin.domain.student.dto.UpdateStudentResponse;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.model.redis.UnAuthenticatedStudentInfo;
import in.koreatech.koin.domain.student.repository.DepartmentRepository;
import in.koreatech.koin.domain.student.repository.MajorRepository;
import in.koreatech.koin.domain.student.repository.StudentRedisRepository;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.student.service.StudentService;
import in.koreatech.koin.domain.student.service.StudentValidationService;
import in.koreatech.koin.domain.user.dto.UserChangePasswordRequest;
import in.koreatech.koin.domain.user.dto.UserChangePasswordSubmitRequest;
import in.koreatech.koin.domain.user.dto.UserFindPasswordRequest;
import in.koreatech.koin.domain.user.model.PasswordResetToken;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserPasswordResetTokenRedisRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.service.RefreshTokenService;
import in.koreatech.koin.domain.user.service.UserService;
import in.koreatech.koin.domain.user.service.UserValidationService;
import in.koreatech.koin.domain.user.verification.service.UserVerificationService;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.unit.fixture.StudentFixture;
import in.koreatech.koin.unit.fixture.UserFixture;

import static in.koreatech.koin.domain.user.model.UserGender.MAN;
import static in.koreatech.koin.domain.user.model.UserIdentity.UNDERGRADUATE;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @InjectMocks
    private StudentService studentService;

    @Mock
    private UserService userService;
    @Mock
    private UserVerificationService userVerificationService;
    @Mock
    private UserValidationService userValidationService;
    @Mock
    private StudentValidationService studentValidationService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private StudentRedisRepository studentRedisRepository;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private DepartmentRepository departmentRepository;
    @Mock
    private MajorRepository majorRepository;
    @Mock
    private GraduationService graduationService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private UserPasswordResetTokenRedisRepository passwordResetTokenRepository;

    @Nested
    class RegisterStudentTest {

        @Test
        void 학생이_가입한다() {
            // given
            String serverURL = "https://koin.test";
            RegisterStudentRequest request = new RegisterStudentRequest(
                "test@koreatech.ac.kr",
                "이름",
                "test_password",
                "닉네임",
                MAN,
                false,
                "컴퓨터공학부",
                "2025100200",
                "01012345678");


            var eventCaptor = ArgumentCaptor.forClass(StudentRegisterRequestEvent.class);
            var infoCaptor = ArgumentCaptor.forClass(UnAuthenticatedStudentInfo.class);

            // when
            studentService.studentRegister(request, serverURL);

            verify(studentValidationService).validateStudentRegister(request);
            verify(studentRedisRepository).save(infoCaptor.capture());
            verify(eventPublisher).publishEvent(eventCaptor.capture());

            var info = infoCaptor.getValue();
            var event = eventCaptor.getValue();

            // then
            assertThat(event.email()).isEqualTo(request.email());
            assertThat(event.serverUrl()).isEqualTo(serverURL);
            assertThat(event.authToken()).isEqualTo(info.getAuthToken());
        }
    }

    @Nested
    class RegisterStudentV2Test {

        private Department department = new Department("컴퓨터공학부");
        private Major major = new Major("컴퓨터공학부", department);

        private Student student;
        private User user = UserFixture.id_설정_코인_유저(1);

        @BeforeEach
        void init() {
            student = Student.builder()
                .studentNumber("2019136135")
                .department(department)
                .major(major)
                .userIdentity(UNDERGRADUATE)
                .isGraduated(false)
                .user(user)
                .build();
        }

        @Test
        void 학생이_가입한다V2() {
            // given
            RegisterStudentRequestV2 request = new RegisterStudentRequestV2(
                "이름",
                "닉네임",
                "test@koreatech.ac.kr",
                "010123456789",
                UserGender.MAN,
                "컴퓨터공학부",
                "2019136135",
                "123",
                "test_password",
                true);

            when(departmentRepository.getByName("컴퓨터공학부")).thenReturn(department);
            when(passwordEncoder.encode("test_password")).thenReturn("ENCODED");

            // when
            studentService.studentRegisterV2(request);

            var registerCaptor = ArgumentCaptor.forClass(StudentRegisterEvent.class);
            verify(eventPublisher).publishEvent(registerCaptor.capture());

            // then
            assertThat(registerCaptor.getValue().phoneNumber())
                .isEqualTo("010123456789");
        }
    }

    @Nested
    class LoginStudentTest {

        @Test
        void 학생이_로그인_하면_lastLoggedAt이_갱신된다() {
            // given
            User user = UserFixture.id_설정_코인_유저(1);
            StudentLoginRequest request = new StudentLoginRequest(
                "test@koreatech.ac.kr",
                "test_password");
            UserAgentInfo agentInfo = UserAgentInfo.builder()
                .model("model")
                .type("WEB")
                .build();

            LocalDateTime lastLoggedAt = LocalDateTime.parse("2025-08-28T00:00:00");

            when(userRepository.getByEmailAndUserTypeIn(
                eq("test@koreatech.ac.kr"),
                eq(UserType.KOIN_STUDENT_TYPES)
            )).thenReturn(user);
            when(passwordEncoder.matches(eq("test_password"), eq(user.getLoginPw())))
                .thenReturn(true);
            when(jwtProvider.createToken(user)).thenReturn("accessToken");
            when(refreshTokenService.createRefreshToken(user.getId(), "WEB")).thenReturn("refreshToken");

            // when
            StudentLoginResponse result = studentService.studentLogin(request, agentInfo);
            LocalDateTime after = user.getLastLoggedAt();

            // then
            assertThat(user.getLastLoggedAt()).isNotNull();
            assertThat(after).isAfter(lastLoggedAt);
            assertThat(result.accessToken()).isEqualTo("accessToken");
            assertThat(result.refreshToken()).isEqualTo("refreshToken");
        }
    }

    @Nested
    class UpdateStudentTest {

        Department department = new Department("컴퓨터공학부");
        Major major = new Major("컴퓨터공학부", department);

        private Student student;

        @BeforeEach
        void init() {
            student = StudentFixture.준호_학생(department, major);
        }

        @Test
        void 학번이_변경되면_학번이_새값으로_설정되고_리셋이_호출된다() {
            // given
            UpdateStudentRequest request = new UpdateStudentRequest(
                UserGender.MAN,
                0,
                false,
                "컴퓨터공학부",
                "new",
                "test_password",
                "닉네임",
                "2023100200",
                "01012345678");

            when(studentRepository.getById(1)).thenReturn(student);
            when(departmentRepository.findByName("컴퓨터공학부")).thenReturn(Optional.of(department));

            // when
            UpdateStudentResponse response = studentService.updateStudent(1, request);

            // then
            assertThat(student.getStudentNumber()).isEqualTo("2023100200");
            assertThat(student.getDepartment()).isSameAs(department);
            assertThat(student.getMajor()).isSameAs(major);
            assertThat(response).isNotNull();
        }

        @Test
        void 학부가_변경되면_학부와_전공이_새값으로_설정되고_리셋이_호출된다() {
            // given
            UpdateStudentRequest request = new UpdateStudentRequest(
                UserGender.MAN,
                0,
                false,
                "기계공학부",
                "new",
                "new_password",
                "닉네임",
                "2019136135",
                "01012345678");

            Department newDept = new Department("컴퓨터공학부");
            Major firstMajor = new Major("소프트웨어", newDept);
            Major otherMajor = new Major("AI", newDept);

            when(studentRepository.getById(1)).thenReturn(student);
            when(departmentRepository.findByName("기계공학부")).thenReturn(Optional.of(newDept));
            when(majorRepository.findByDepartmentId(newDept.getId())).thenReturn(List.of(firstMajor, otherMajor));

            // when
            UpdateStudentResponse result = studentService.updateStudent(1, request);

            // then
            assertThat(student.getDepartment()).isSameAs(newDept);
            assertThat(student.getMajor()).isSameAs(firstMajor);
            assertThat(student.getStudentNumber()).isEqualTo("2019136135");
            assertThat(result).isNotNull();
        }
    }

    @Nested
    class UpdateStudentV2Test {

        Department department = new Department("컴퓨터공학부");
        Major major = new Major("컴퓨터공학부", department);

        private Student student;

        @BeforeEach
        void init() {
            student = StudentFixture.준호_학생(department, major);
        }

        @Test
        void 학번이_변경되면_학번이_새값으로_설정되고_리셋이_호출된다V2() {
            // given
            UpdateStudentRequestV2 request = new UpdateStudentRequestV2(
                "new",
                "닉네임",
                "test@koreatech.ac.kr",
                "01012345678",
                UserGender.MAN,
                "컴퓨터공학부",
                "2023100200",
                "new_password");

            when(studentRepository.getById(1)).thenReturn(student);
            when(departmentRepository.findByName("컴퓨터공학부")).thenReturn(Optional.of(department));

            // when
            studentService.updateStudentV2(1, request);

            // then
            assertThat(student.getStudentNumber()).isEqualTo("2023100200");
            assertThat(student.getDepartment()).isSameAs(department);
            assertThat(student.getMajor()).isSameAs(major);
        }

        @Test
        void 학부가_변경되면_학부와_전공이_새값으로_설정되고_리셋이_호출된다V2() {
            // given
            UpdateStudentRequestV2 request = new UpdateStudentRequestV2(
                "new",
                "닉네임",
                "test@koreatech.ac.kr",
                "01012345678",
                UserGender.MAN,
                "기계공학부",
                "2019136135",
                "new_password");

            Department newDept = new Department("컴퓨터공학부");
            Major firstMajor = new Major("소프트웨어", newDept);
            Major otherMajor = new Major("AI", newDept);

            when(studentRepository.getById(1)).thenReturn(student);
            when(departmentRepository.findByName("기계공학부")).thenReturn(Optional.of(newDept));
            when(majorRepository.findByDepartmentId(newDept.getId())).thenReturn(List.of(firstMajor, otherMajor));

            // when
            studentService.updateStudentV2(1, request);

            // then
            assertThat(student.getStudentNumber()).isEqualTo("2019136135");
            assertThat(student.getDepartment()).isSameAs(newDept);
            assertThat(student.getMajor()).isSameAs(firstMajor);
        }
    }

    @Nested
    class findPasswordTest {

        @Test
        void 비밀번호_찾기를_성공한다() {
            // given
            User user = UserFixture.id_설정_코인_유저(1);
            String serverURL = "https://koin.test";
            UserFindPasswordRequest request = new UserFindPasswordRequest("test@koreatech.ac.kr");
            when(userRepository.getByEmailAndUserTypeIn(
                eq(request.email()),
                eq(UserType.KOIN_STUDENT_TYPES)))
                .thenReturn(user);

            // when
            studentService.findPassword(request, serverURL);

            var eventCaptor = ArgumentCaptor.forClass(StudentFindPasswordEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            var event = eventCaptor.getValue();

            // then
            assertThat(event.email()).isEqualTo("test@koreatech.ac.kr");
            assertThat(event.serverUrl()).isEqualTo(serverURL);
            assertThat(event.resetToken()).isNotNull();
        }
    }

    @Nested
    class StudentGetTest {

        Department department = new Department("컴퓨터공학부");
        Major major = new Major("컴퓨터공학부", department);

        @Test
        void 학생을_조회한다() {
            // given
            Integer userId = 1;
            Student student = StudentFixture.준호_학생(department, major);
            when(studentRepository.getById(userId)).thenReturn(student);

            // when
            StudentResponse response = studentService.getStudent(userId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.email()).isEqualTo(student.getUser().getEmail());
            assertThat(response.studentNumber()).isEqualTo(student.getStudentNumber());
        }

        @Test
        void 학생_학업정보를_조회한다() {
            // given
            Integer userId = 1;
            Student student = StudentFixture.준호_학생(department, major);
            when(studentRepository.getById(userId)).thenReturn(student);

            // when
            StudentWithAcademicResponse response = studentService.getStudentWithAcademicInfo(userId);

            // then
            assertThat(response).isNotNull();
            assertThat(response.email()).isEqualTo(student.getUser().getEmail());
            assertThat(response.studentNumber()).isEqualTo(student.getStudentNumber());
        }
    }

    @Nested
    class ChangePasswordTest { //TODO: 실객체 사용

        Department department = new Department("컴퓨터공학부");
        Major major = new Major("컴퓨터공학부", department);

        @Test
        void 학생이_패스워드를_변경한다() {
            // given
            User user = UserFixture.코인_유저();
            Student studentA = Student.builder()
                .studentNumber("2019136135")
                .department(department)
                .major(major)
                .userIdentity(UNDERGRADUATE)
                .isGraduated(false)
                .user(user)
                .build();
            Integer userId = user.getId();
            UserChangePasswordRequest request = new UserChangePasswordRequest("new_password");
            when(studentRepository.getById(userId)).thenReturn(studentA);
            when(passwordEncoder.encode("new_password")).thenReturn("new_password");

            // when
            studentService.changePassword(userId, request);

            // then
            assertThat(user.getLoginPw()).isEqualTo("new_password");
        }
    }

    @Nested
    class changePasswordSubmitTest {

        @Test
        void 토큰을_통해_비밀번호를_변경한다() {
            // given
            User user = UserFixture.코인_유저();
            String resetToken = "reset_token";

            UserChangePasswordSubmitRequest request = new UserChangePasswordSubmitRequest("new_password");
            PasswordResetToken passwordResetToken = mock(PasswordResetToken.class);

            when(passwordResetTokenRepository.getByResetToken(resetToken)).thenReturn(passwordResetToken);
            when(userRepository.getById(passwordResetToken.getId())).thenReturn(user);
            when(passwordEncoder.encode("new_password")).thenReturn("new_password");

            // when
            studentService.changePasswordSubmit(request, resetToken);

            // then
            assertThat(user.getLoginPw()).isEqualTo("new_password");
        }
    }

    @Nested
    class CheckResetTokenTest {

        @Test
        void 리셋토큰_뷰와_모델이_올바르게_세팅된다() {
            // given
            String token = "token";
            String serverUrl = "https://koin.test";

            // when
            ModelAndView modelAndView = studentService.checkResetToken(token, serverUrl);

            // then
            assertThat(modelAndView.getViewName()).isEqualTo("change_password_config");
            assertThat(modelAndView.getModel())
                .containsEntry("contextPath", serverUrl)
                .containsEntry("resetToken", token);
        }
    }
}
