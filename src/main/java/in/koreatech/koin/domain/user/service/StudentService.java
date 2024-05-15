package in.koreatech.koin.domain.user.service;

import java.time.Clock;
import java.util.Optional;
import java.util.UUID;

import org.joda.time.LocalDateTime;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import in.koreatech.koin.domain.user.dto.AuthTokenRequest;
import in.koreatech.koin.domain.user.dto.AuthTokenResponse;
import in.koreatech.koin.domain.user.dto.FindPasswordRequest;
import in.koreatech.koin.domain.user.dto.StudentRegisterRequest;
import in.koreatech.koin.domain.user.dto.StudentResponse;
import in.koreatech.koin.domain.user.dto.StudentUpdateRequest;
import in.koreatech.koin.domain.user.dto.StudentUpdateResponse;
import in.koreatech.koin.domain.user.dto.UserPasswordChangeRequest;
import in.koreatech.koin.domain.user.exception.DuplicationNicknameException;
import in.koreatech.koin.domain.user.exception.StudentDepartmentNotValidException;
import in.koreatech.koin.domain.user.exception.StudentNumberNotValidException;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.StudentDepartment;
import in.koreatech.koin.domain.user.model.StudentEmailRequestEvent;
import in.koreatech.koin.domain.user.model.StudentRegisterEvent;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.model.redis.StudentTemporaryStatus;
import in.koreatech.koin.domain.user.repository.StudentRedisRepository;
import in.koreatech.koin.domain.user.repository.StudentRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.domain.email.exception.DuplicationEmailException;
import in.koreatech.koin.global.domain.email.form.StudentPasswordChangeData;
import in.koreatech.koin.global.domain.email.form.StudentRegistrationData;
import in.koreatech.koin.global.domain.email.model.EmailAddress;
import in.koreatech.koin.global.domain.email.service.MailService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRedisRepository studentRedisRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public StudentResponse getStudent(Integer userId) {
        Student student = studentRepository.getById(userId);
        return StudentResponse.from(student);
    }

    @Transactional
    public StudentUpdateResponse updateStudent(Integer userId, StudentUpdateRequest request) {
        Student student = studentRepository.getById(userId);
        User user = student.getUser();
        checkNicknameDuplication(request.nickname(), userId);
        checkDepartmentValid(request.major());
        user.update(request.nickname(), request.name(),
            request.phoneNumber(), UserGender.from(request.gender()));
        user.updateStudentPassword(passwordEncoder, request.password());
        student.update(request.studentNumber(), request.major());
        studentRepository.save(student);

        return StudentUpdateResponse.from(student);
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

    @Transactional
    public ModelAndView authenticate(AuthTokenRequest request) {
        Optional<StudentTemporaryStatus> studentTemporaryStatus = studentRedisRepository.findById(request.authToken());

        if (studentTemporaryStatus.isEmpty()) {
            ModelAndView modelAndView = new ModelAndView("error_config");
            modelAndView.addObject("errorMessage", "토큰에 해당하는 사용자를 찾을 수 없습니다.");
            return modelAndView;
        }

        Student student = studentTemporaryStatus.get().toStudent(passwordEncoder);
        Optional<User> foundUser = userRepository.findByEmail(student.getUser().getEmail());

        if (foundUser.isPresent()) {
            ModelAndView modelAndView = new ModelAndView("error_config");
            modelAndView.addObject("errorMessage", "이미 인증된 사용자입니다.");
            return modelAndView;
        }

        studentRepository.save(student);
        userRepository.save(student.getUser());

        eventPublisher.publishEvent(new StudentRegisterEvent(student.getUser().getEmail()));

        return new ModelAndView("success_register_config");
    }

    @Transactional
    public AuthTokenResponse studentRegister(StudentRegisterRequest request, String serverURL) {

        validateStudentRegister(request);
        String authToken = UUID.randomUUID().toString();

        StudentTemporaryStatus studentTemporaryStatus = StudentTemporaryStatus.of(request, authToken);
        studentRedisRepository.save(studentTemporaryStatus);

        mailService.sendMail(request.email(), new StudentRegistrationData(serverURL, authToken));
        eventPublisher.publishEvent(new StudentEmailRequestEvent(request.email()));

        return AuthTokenResponse.from(authToken);
    }

    private void validateStudentRegister(StudentRegisterRequest request) {
        EmailAddress emailAddress = EmailAddress.from(request.email());
        emailAddress.validateKoreatechEmail();

        validateDataExist(request);
        validateStudentNumber(request.studentNumber());
        checkDepartmentValid(request.department());
    }

    private void validateDataExist(StudentRegisterRequest request) {
        userRepository.findByEmail(request.email())
            .ifPresent(user -> {
                throw DuplicationEmailException.withDetail("email: " + request.email());
            });

        if (request.nickname() != null) {
            userRepository.findByNickname(request.nickname())
                .ifPresent(user -> {
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
    public void changePassword(UserPasswordChangeRequest request, String resetToken) {
        User authedUser = userRepository.getByResetToken(resetToken);
        authedUser.validateResetToken();
        authedUser.updatePassword(passwordEncoder, request.password());
        userRepository.save(authedUser);
    }
}
