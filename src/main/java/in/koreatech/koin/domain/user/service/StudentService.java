package in.koreatech.koin.domain.user.service;

import java.time.Clock;
import java.util.Optional;

import org.joda.time.LocalDateTime;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import in.koreatech.koin.domain.user.dto.AuthTokenRequest;
import in.koreatech.koin.domain.user.dto.StudentRegisterRequest;
import in.koreatech.koin.domain.user.dto.FindPasswordRequest;
import in.koreatech.koin.domain.user.dto.StudentResponse;
import in.koreatech.koin.domain.user.dto.StudentUpdateRequest;
import in.koreatech.koin.domain.user.dto.StudentUpdateResponse;
import in.koreatech.koin.domain.user.exception.DuplicationNicknameException;
import in.koreatech.koin.domain.user.exception.StudentDepartmentNotValidException;
import in.koreatech.koin.domain.user.exception.StudentNumberNotValidException;
import in.koreatech.koin.domain.user.model.AuthResult;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.StudentDepartment;
import in.koreatech.koin.domain.user.model.StudentEmailRequestEvent;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.repository.StudentRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.domain.email.exception.DuplicationEmailException;
import in.koreatech.koin.global.domain.email.form.StudentRegistrationData;
import in.koreatech.koin.global.domain.email.model.EmailAddress;
import in.koreatech.koin.global.domain.email.service.MailService;
import in.koreatech.koin.global.domain.email.form.StudentPasswordChangeData;
import in.koreatech.koin.global.domain.email.service.MailService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    public StudentResponse getStudent(Long userId) {
        Student student = studentRepository.getById(userId);
        return StudentResponse.from(student);
    }

    @Transactional
    public StudentUpdateResponse updateStudent(Long userId, StudentUpdateRequest request) {
        Student student = studentRepository.getById(userId);
        User user = student.getUser();
        checkNicknameDuplication(request.nickname());
        checkDepartmentValid(request.major());
        user.update(request.nickname(), request.name(),
            request.phoneNumber(), UserGender.from(request.gender()));
        student.update(request.studentNumber(), request.major());
        studentRepository.save(student);

        return StudentUpdateResponse.from(student);
    }

    public void checkNicknameDuplication(String nickname) {
        if (nickname != null && userRepository.existsByNickname(nickname)) {
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
        Optional<User> user = userRepository.findByAuthToken(request.authToken());
        return new AuthResult(user, eventPublisher, clock).toModelAndViewForStudent();
    }

    @Transactional
    public void studentRegister(StudentRegisterRequest request, String serverURL) {
        Student student = request.toStudent(passwordEncoder, clock);

        validateStudentRegister(student);

        studentRepository.save(student);
        userRepository.save(student.getUser());

        mailService.sendMail(request.email(), new StudentRegistrationData(serverURL, student.getUser().getAuthToken()));
        eventPublisher.publishEvent(new StudentEmailRequestEvent(request.email()));
    }

    private void validateStudentRegister(Student student) {
        EmailAddress emailAddress = EmailAddress.from(student.getUser().getEmail());
        emailAddress.validateKoreatechEmail();

        validateDataExist(student);
        validateStudentNumber(student.getStudentNumber());
        checkDepartmentValid(student.getDepartment());
    }

    private void validateDataExist(Student student) {
        userRepository.findByEmail(student.getUser().getEmail())
            .ifPresent(user -> {
                throw DuplicationEmailException.withDetail("email: " + student.getUser().getEmail());
            });

        if (student.getUser().getNickname() != null) {
            userRepository.findByNickname(student.getUser().getNickname())
                .ifPresent(user -> {
                    throw DuplicationNicknameException.withDetail("nickname: " + student.getUser().getNickname());
                });
        }
    }

    private void validateStudentNumber(String studentNumber) {
        if (studentNumber == null) {
            return;
        }
        Integer studentNumberYear = Student.parseStudentNumberYear(studentNumber);
        if (studentNumberYear < 1992
            || new LocalDateTime().now().getYear() < studentNumberYear) {
            throw StudentNumberNotValidException.withDetail("studentNumber: " + studentNumber);
        }
    }

    @Transactional
    public void findPassword(FindPasswordRequest request, String serverURL) {
        User user = userRepository.getByEmail(request.email());
        user.generateResetTokenForFindPassword();
        User authedUser = userRepository.save(user);
        mailService.sendMail(request.email(), new StudentPasswordChangeData(serverURL, authedUser.getResetToken()));
    }

    public String checkResetToken(String resetToken) {
        User user = userRepository.getByResetToken(resetToken);
        return "change_password_config.html";
    }

    @Transactional
    public boolean changePassword(String password, String resetToken) {
        User authedUser = userRepository.getByResetToken(resetToken);
        authedUser.updatePassword(passwordEncoder, password);
        userRepository.save(authedUser);
        return true;
    }
}
