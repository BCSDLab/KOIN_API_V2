package in.koreatech.koin.domain.user.service;

import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import in.koreatech.koin.domain.user.dto.AuthResponse;
import in.koreatech.koin.domain.user.dto.AuthTokenRequest;
import in.koreatech.koin.domain.user.dto.StudentRegisterRequest;
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
import in.koreatech.koin.domain.user.model.StudentRegisterEvent;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.repository.StudentRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.domain.email.exception.DuplicationEmailException;
import in.koreatech.koin.global.domain.email.form.StudentRegistrationData;
import in.koreatech.koin.global.domain.email.model.EmailAddress;
import in.koreatech.koin.global.domain.email.service.MailService;
import jakarta.servlet.http.HttpServletRequest;
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

    public static final String MAIL_ERROR_CONFIG = "error_config";
    public static final String MODEL_KEY_ERROR_MESSAGE = "errorMessage";
    public static final String MAIL_SUCCESS_REGISTER_CONFIG = "success_register_config";

    public StudentResponse getStudent(Long userId) {
        Student student = studentRepository.getById(userId);
        return StudentResponse.from(student);
    }

    @Transactional
    public StudentUpdateResponse updateStudent(Long userId, StudentUpdateRequest request) {
        Student student = studentRepository.getById(userId);
        User user = student.getUser();
        CheckNicknameDuplication(request.nickname());
        CheckDepartmentValid(request.major());
        user.update(request.nickname(), request.name(),
            request.phoneNumber(), UserGender.from(request.gender()));
        student.update(request.studentNumber(), request.major());
        studentRepository.save(student);

        return StudentUpdateResponse.from(student);
    }

    public void CheckNicknameDuplication(String nickname) {
        if (nickname != null && userRepository.existsByNickname(nickname)) {
            throw DuplicationNicknameException.withDetail("nickname : " + nickname);
        }
    }

    public void CheckDepartmentValid(String department) {
        if (department != null & !StudentDepartment.isValid(department)) {
            throw StudentDepartmentNotValidException.withDetail("학부(학과) : " + department);
        }
    }

    @Transactional
    public AuthResponse authenticate(AuthTokenRequest request) {

        Optional<User> user = userRepository.findByAuthToken(request.authToken());

        AuthResult authResult = AuthResult.from(user);

        if (authResult.isSuccess()) {
            user.get().enrichForAuthed();
            eventPublisher.publishEvent(new StudentRegisterEvent(user.get().getEmail()));
        }

        return authResult.toAuthResponse();
    }

    @Transactional
    public void StudentRegister(StudentRegisterRequest request, HttpServletRequest httpServletRequest) {
        String host = getHost(httpServletRequest);
        Student student = request.toStudent(passwordEncoder);

        validateStudentRegister(student);

        studentRepository.save(student);
        userRepository.save(student.getUser());

        mailService.sendMail(request.email(), new StudentRegistrationData(host, student.getUser().getAuthToken()));
        eventPublisher.publishEvent(new StudentEmailRequestEvent(request.email()));
    }

    public ModelAndView makeModelAndViewForStudent(AuthResponse authResponse) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName(makeViewNameFor(authResponse));

        if (!authResponse.isSuccess()) {
            modelAndView.addObject(MODEL_KEY_ERROR_MESSAGE, authResponse.errorMessage());
        }

        return modelAndView;
    }

    private static String makeViewNameFor(AuthResponse authResponse) {
        if (!authResponse.isSuccess()) {
            return MAIL_ERROR_CONFIG;
        }
        return MAIL_SUCCESS_REGISTER_CONFIG;
    }

    private void validateStudentRegister(Student student) {
        EmailAddress emailAddress = EmailAddress.from(student.getUser().getEmail());
        emailAddress.validatePortalEmail();

        validateDataExist(student);
        validateStudentNumber(student);
        validateDepartment(student);
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

    private void validateStudentNumber(Student student) {
        if (student.getStudentNumber() == null) {
            return;
        }
        if (!student.isStudentNumberValidated()) {
            throw StudentNumberNotValidException.withDetail("studentNumber: " + student.getStudentNumber());
        }
    }

    private void validateDepartment(Student student) {
        if (student.getDepartment() == null) {
            return;
        }
        if (!student.isDeptValidated()) {
            throw StudentNumberNotValidException.withDetail("department: " + student.getDepartment());
        }
    }

    private String getHost(HttpServletRequest request) {
        String schema = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        StringBuilder url = new StringBuilder();
        url.append(schema).append("://");
        url.append(serverName);

        if (serverPort != 80 && serverPort != 443) {
            url.append(":").append(serverPort);
        }

        return url.toString();
    }
}

