package in.koreatech.koin.domain.student.service;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.model.StudentDepartment;
import in.koreatech.koin.domain.student.model.StudentEmailRequestEvent;
import in.koreatech.koin.domain.student.model.StudentRegisterEvent;
import in.koreatech.koin.domain.user.dto.UserPasswordChangeRequest;
import in.koreatech.koin.domain.user.model.*;
import in.koreatech.koin.domain.student.model.redis.StudentTemporaryStatus;
import in.koreatech.koin.domain.student.repository.StudentRedisRepository;

import org.joda.time.LocalDateTime;
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
import in.koreatech.koin.domain.user.exception.DuplicationNicknameException;
import in.koreatech.koin.domain.student.exception.StudentDepartmentNotValidException;
import in.koreatech.koin.domain.student.exception.StudentNumberNotValidException;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.repository.UserTokenRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.concurrent.ConcurrencyGuard;
import in.koreatech.koin.global.domain.email.exception.DuplicationEmailException;
import in.koreatech.koin.global.domain.email.form.StudentPasswordChangeData;
import in.koreatech.koin.global.domain.email.form.StudentRegistrationData;
import in.koreatech.koin.global.domain.email.model.EmailAddress;
import in.koreatech.koin.global.domain.email.service.MailService;
import in.koreatech.koin.global.domain.notification.model.NotificationSubscribeType;
import in.koreatech.koin.global.domain.notification.service.NotificationService;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentRedisRepository studentRedisRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;
    private final UserTokenRepository userTokenRepository;
    private final JwtProvider jwtProvider;

    public StudentResponse getStudent(Integer userId) {
        Student student = studentRepository.getById(userId);
        return StudentResponse.from(student);
    }

    @Transactional
    public StudentLoginResponse studentLogin(StudentLoginRequest request) {
        User user = userRepository.getByEmail(request.email());
        Optional<StudentTemporaryStatus> studentTemporaryStatus = studentRedisRepository.findById(request.email());

        if (!user.isSamePassword(passwordEncoder, request.password())) {
            throw new KoinIllegalArgumentException("비밀번호가 틀렸습니다.");
        }

        if (studentTemporaryStatus.isPresent()) {
            throw new AuthorizationException("미인증 상태입니다. 아우누리에서 인증메일을 확인해주세요");
        }

        String accessToken = jwtProvider.createToken(user);
        String refreshToken = String.format("%s-%d", UUID.randomUUID(), user.getId());
        UserToken savedToken = userTokenRepository.save(UserToken.create(user.getId(), refreshToken));
        user.updateLastLoggedTime(java.time.LocalDateTime.now());

        return StudentLoginResponse.of(accessToken, savedToken.getRefreshToken());
    }

    @Transactional
    public StudentUpdateResponse updateStudent(Integer userId, StudentUpdateRequest request) {
        Student student = studentRepository.getById(userId);
        User user = student.getUser();
        checkNicknameDuplication(request.nickname(), userId);
        checkDepartmentValid(request.major());
        updateUserDetails(user, request);
        user.updateStudentPassword(passwordEncoder, request.password());
        student.update(request.studentNumber(), request.major());
        studentRepository.save(student);
        return StudentUpdateResponse.from(student);
    }

    private void updateUserDetails(User user, StudentUpdateRequest request) {
        user.update(request.nickname(), request.name(), request.phoneNumber(), request.gender());
    }

    public void checkNicknameDuplication(String nickname, Integer userId) {
        User checkUser = userRepository.getById(userId);
        if (nickname != null && !nickname.equals(checkUser.getNickname())
            && userRepository.existsByNickname(nickname)) {
            throw DuplicationNicknameException.withDetail("nickname : " + nickname);
        }
    }

    public void checkDepartmentValid(String department) {
        if (department != null && !StudentDepartment.isValid(department)) {
            throw StudentDepartmentNotValidException.withDetail("학부(학과) : " + department);
        }
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

        notificationService.permitNotificationSubscribe(student.getId(), NotificationSubscribeType.REVIEW_PROMPT);

        return new ModelAndView("success_register_config");
    }

    @Transactional
    public void studentRegister(StudentRegisterRequest request, String serverURL) {

        validateStudentRegister(request);
        String authToken = UUID.randomUUID().toString();

        StudentTemporaryStatus studentTemporaryStatus = StudentTemporaryStatus.of(request, authToken);
        studentRedisRepository.save(studentTemporaryStatus);

        mailService.sendMail(request.email(), new StudentRegistrationData(serverURL, authToken));
        eventPublisher.publishEvent(new StudentEmailRequestEvent(request.email()));
    }

    private void validateStudentRegister(StudentRegisterRequest request) {
        EmailAddress emailAddress = EmailAddress.from(request.email());
        emailAddress.validateKoreatechEmail();

        validateDataExist(request);
        validateStudentNumber(request.studentNumber());
        checkDepartmentValid(request.major());
    }

    private void validateDataExist(StudentRegisterRequest request) {
        userRepository.findByEmail(request.email())
            .ifPresent(user -> {
                throw DuplicationEmailException.withDetail("email: " + request.email());
            });
        studentRedisRepository.findById(request.email())
            .ifPresent(studentTemporaryStatus -> {
                throw DuplicationEmailException.withDetail("email: " + request.email());
            });

        if (request.nickname() != null) {
            userRepository.findByNickname(request.nickname())
                .ifPresent(user -> {
                    throw DuplicationNicknameException.withDetail("nickname: " + request.nickname());
                });
            studentRedisRepository.findByNickname(request.nickname())
                .ifPresent(studentTemporaryStatus -> {
                    throw DuplicationNicknameException.withDetail("nickname: " + request.nickname());
                });
        }
    }

    private void validateStudentNumber(String studentNumber) {
        if (studentNumber == null) {
            return;
        }
        int studentNumberYear = Student.parseStudentNumberYear(studentNumber);
        if (studentNumberYear < 1992
            || LocalDateTime.now().getYear() < studentNumberYear) {
            throw StudentNumberNotValidException.withDetail("studentNumber: " + studentNumber);
        }
    }

    @Transactional
    public void findPassword(FindPasswordRequest request, String serverURL) {
        User user = userRepository.getByEmail(request.email());
        user.generateResetTokenForFindPassword(clock);
        User authedUser = userRepository.save(user);
        mailService.sendMail(request.email(), new StudentPasswordChangeData(serverURL, authedUser.getResetToken()));
    }

    public ModelAndView checkResetToken(String resetToken, String serverUrl) {
        ModelAndView modelAndView = new ModelAndView("change_password_config");
        modelAndView.addObject("contextPath", serverUrl);
        modelAndView.addObject("resetToken", resetToken);
        return modelAndView;
    }

    @Transactional
    public void changePassword(Integer userId, UserPasswordChangeRequest request) {
        Student student = studentRepository.getById(userId);
        User user = student.getUser();
        user.updateStudentPassword(passwordEncoder, request.password());
    }

    @Transactional
    public void changePasswordSubmit(UserPasswordChangeSubmitRequest request, String resetToken) {
        User authedUser = userRepository.getByResetToken(resetToken);
        authedUser.validateResetToken();
        authedUser.updatePassword(passwordEncoder, request.password());
        userRepository.save(authedUser);
    }
}
