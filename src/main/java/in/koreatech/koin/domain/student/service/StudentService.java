package in.koreatech.koin.domain.student.service;
import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.model.StudentEmailRequestEvent;
import in.koreatech.koin.domain.student.model.StudentRegisterEvent;
import in.koreatech.koin.domain.user.dto.UserPasswordChangeRequest;
import in.koreatech.koin.domain.user.model.*;
import in.koreatech.koin.domain.student.model.redis.StudentTemporaryStatus;
import in.koreatech.koin.domain.student.repository.StudentRedisRepository;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;
import in.koreatech.koin.domain.user.dto.AuthTokenRequest;
import in.koreatech.koin.domain.user.dto.FindPasswordRequest;
import in.koreatech.koin.domain.student.dto.StudentLoginRequest;
import in.koreatech.koin.domain.student.dto.StudentLoginResponse;
import in.koreatech.koin.domain.student.dto.StudentRegisterRequest;
import in.koreatech.koin.domain.student.dto.StudentResponse;
import in.koreatech.koin.domain.student.dto.StudentUpdateRequest;
import in.koreatech.koin.domain.student.dto.StudentUpdateResponse;
import in.koreatech.koin.domain.user.dto.UserPasswordChangeSubmitRequest;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.repository.UserTokenRepository;
import in.koreatech.koin.domain.user.service.UserService;
import in.koreatech.koin.domain.user.service.UserTokenService;
import in.koreatech.koin.domain.user.service.UserValidationService;
import in.koreatech.koin.global.concurrent.ConcurrencyGuard;
import in.koreatech.koin.global.domain.email.form.StudentPasswordChangeData;
import in.koreatech.koin.global.domain.email.form.StudentRegistrationData;
import in.koreatech.koin.global.domain.email.service.MailService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final MailService mailService;
    private final UserService userService;
    private final UserValidationService userValidationService;
    private final StudentValidationService studentValidationService;
    private final UserRepository userRepository;
    private final UserTokenService userTokenService;
    private final UserTokenRepository userTokenRepository;
    private final StudentRepository studentRepository;
    private final StudentRedisRepository studentRedisRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    public void studentRegister(StudentRegisterRequest request, String serverURL) {
        studentValidationService.validateStudentRegister(request);
        String authToken = UUID.randomUUID().toString();

        StudentTemporaryStatus studentTemporaryStatus = StudentTemporaryStatus.of(request, authToken);
        studentRedisRepository.save(studentTemporaryStatus);

        mailService.sendMail(request.email(), new StudentRegistrationData(serverURL, authToken));
        eventPublisher.publishEvent(new StudentEmailRequestEvent(request.email()));
    }

    @Transactional
    public StudentLoginResponse studentLogin(StudentLoginRequest request) {
        User user = userValidationService.checkLoginCredentials(request.email(), request.password());
        userValidationService.checkUserAuthentication(request.email());

        String accessToken = userTokenService.createAccessToken(user);
        String refreshToken = userTokenService.generateRefreshToken(user);
        UserToken savedToken = userTokenRepository.save(UserToken.create(user.getId(), refreshToken));
        userService.updateLastLoginTime(user);

        return StudentLoginResponse.of(accessToken, savedToken.getRefreshToken());
    }

    @Transactional
    public StudentUpdateResponse updateStudent(Integer userId, StudentUpdateRequest request) {
        studentValidationService.validateUpdateNickname(request.nickname(), userId);
        studentValidationService.validateDepartment(request.major());

        Student student = studentRepository.getById(userId);
        User user = student.getUser();

        user.update(request.nickname(), request.name(), request.phoneNumber(), request.gender());
        user.updateStudentPassword(passwordEncoder, request.password());
        student.updateInfo(request.studentNumber(), request.major());

        return StudentUpdateResponse.from(student);
    }

    @ConcurrencyGuard(lockName = "studentAuthenticate")
    public ModelAndView authenticate(AuthTokenRequest request) {
        Optional<StudentTemporaryStatus> studentTemporaryStatus = studentRedisRepository.findByAuthToken(
            request.authToken());
        if (studentTemporaryStatus.isEmpty()) {
            ModelAndView modelAndView = new ModelAndView("error_config");
            modelAndView.addObject("errorMessage", "토큰이 유효하지 않습니다.");
            return modelAndView;
        }
        Student student = studentTemporaryStatus.get().toStudent(passwordEncoder);
        studentRepository.save(student);
        userRepository.save(student.getUser());
        studentRedisRepository.deleteById(student.getUser().getEmail());
        eventPublisher.publishEvent(new StudentRegisterEvent(student.getUser().getEmail()));
        return new ModelAndView("success_register_config");
    }

    @Transactional
    public void findPassword(FindPasswordRequest request, String serverURL) {
        User user = userRepository.getByEmail(request.email());
        user.generateResetTokenForFindPassword(clock);
        User authedUser = userRepository.save(user);
        mailService.sendMail(request.email(), new StudentPasswordChangeData(serverURL, authedUser.getResetToken()));
    }

    public StudentResponse getStudent(Integer userId) {
        Student student = studentRepository.getById(userId);
        return StudentResponse.from(student);
    }

    @Transactional
    public void changePassword(Integer userId, UserPasswordChangeRequest request) {
        Student student = studentRepository.getById(userId);
        User user = student.getUser();
        user.updatePassword(passwordEncoder, request.password());
    }

    @Transactional
    public void changePasswordSubmit(UserPasswordChangeSubmitRequest request, String resetToken) {
        User authedUser = userRepository.getByResetToken(resetToken);
        authedUser.validateResetToken();
        authedUser.updatePassword(passwordEncoder, request.password());
    }

    public ModelAndView checkResetToken(String resetToken, String serverUrl) {
        ModelAndView modelAndView = new ModelAndView("change_password_config");
        modelAndView.addObject("contextPath", serverUrl);
        modelAndView.addObject("resetToken", resetToken);
        return modelAndView;
    }
}
