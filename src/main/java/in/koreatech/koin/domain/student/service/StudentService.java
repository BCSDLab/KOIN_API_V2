package in.koreatech.koin.domain.student.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import in.koreatech.koin.domain.graduation.service.GraduationService;
import in.koreatech.koin.domain.student.dto.StudentAcademicInfoUpdateRequest;
import in.koreatech.koin.domain.student.dto.StudentAcademicInfoUpdateResponse;
import in.koreatech.koin.domain.student.dto.StudentLoginRequest;
import in.koreatech.koin.domain.student.dto.StudentLoginResponse;
import in.koreatech.koin.domain.student.dto.StudentRegisterRequest;
import in.koreatech.koin.domain.student.dto.StudentResponse;
import in.koreatech.koin.domain.student.dto.StudentUpdateRequest;
import in.koreatech.koin.domain.student.dto.StudentUpdateResponse;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.model.StudentEmailRequestEvent;
import in.koreatech.koin.domain.student.model.StudentRegisterEvent;
import in.koreatech.koin.domain.student.model.redis.StudentTemporaryStatus;
import in.koreatech.koin.domain.student.repository.DepartmentRepository;
import in.koreatech.koin.domain.student.repository.MajorRepository;
import in.koreatech.koin.domain.student.repository.StudentRedisRepository;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.user.dto.AuthTokenRequest;
import in.koreatech.koin.domain.user.dto.FindPasswordRequest;
import in.koreatech.koin.domain.user.dto.UserPasswordChangeRequest;
import in.koreatech.koin.domain.user.dto.UserPasswordChangeSubmitRequest;
import in.koreatech.koin.domain.user.model.PasswordResetToken;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.repository.UserPasswordResetTokenRepository;
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
    private final DepartmentRepository departmentRepository;
    private final MajorRepository majorRepository;
    private final GraduationService graduationService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final UserPasswordResetTokenRepository passwordResetTokenRepository;

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

        // 학번에 변경 사항이 생겼을 경우
        String oldStudentNumber = student.getStudentNumber();
        String newStudentNumber = request.studentNumber();

        boolean updateStudentNumber = isChangeStudentNumber(oldStudentNumber, newStudentNumber);
        if (updateStudentNumber) {
            student.updateStudentNumber(newStudentNumber);
        }

        // Department 조회
        Department newDepartment = null;
        if (request.major() != null) {
            newDepartment = departmentRepository.getByName(request.major());
        }
        Department oldDepartment = student.getDepartment();

        /**
         * 해당 API에서는 학생의 Major를 수정할 수 없음.
         * 졸업학점계산기 설계 상으로 학번, 학부, 전공이 변경되면 졸업학점 관련 메소드를 호출해야함.
         * Department로 조회된 Major의 첫 번째 값을 Student의 Major으로 설정
         * 단, Department가 변경될 경우만 설정
         */
        Major newMajor = null;
        boolean updateDepartment = isChangedDepartment(oldDepartment, newDepartment);
        if (updateDepartment) {
            List<Major> majors = majorRepository.findByDepartmentId(newDepartment.getId());
            newMajor = majors.get(0);
            student.updateDepartmentMajor(newDepartment, newMajor);
        } else if (student.getDepartment() != null && student.getMajor() == null) {
            List<Major> majors = majorRepository.findByDepartmentId(student.getDepartment().getId());
            newMajor = majors.get(0);
            student.updateDepartmentMajor(student.getDepartment(), newMajor);
        }

        /**
         * 1. 학생의 학변이 변경됐고, 학부 변경이 없는경우 (학부가 있냐 / 학부가 없냐)
         * 2. 학생의 학번이 변경이 안되고, 학부 변경이 있는 경우 (학번이 있냐 / 학번이 없냐)
         * 3. 학생의 학번도 변경되고, 학부 변경도 있는 경우
         */
        if (updateStudentNumber && updateDepartment) {
            graduationService.resetStudentCourseCalculation(student, newMajor);
        }
        else if (updateDepartment) {
            if (student.getStudentNumber() != null) {
                graduationService.resetStudentCourseCalculation(student, newMajor);
            }
        }
        else if (updateStudentNumber) {
            if (student.getDepartment() != null) {
                graduationService.resetStudentCourseCalculation(student, newMajor);
            }
        }

        user.update(request.nickname(), request.name(), request.phoneNumber(), request.gender());
        user.updateStudentPassword(passwordEncoder, request.password());

        return StudentUpdateResponse.from(student);
    }

    private boolean isChangeStudentNumber(String newStudentNumber, String oldStudentNumber) {
        return newStudentNumber != null && !newStudentNumber.equals(oldStudentNumber);
    }

    private boolean isChangedDepartment(Department oldDepartment, Department newDepartment) {
        return newDepartment != null && !newDepartment.equals(oldDepartment);
    }

    @Transactional
    public StudentAcademicInfoUpdateResponse updateStudentAcademicInfo(Integer userId, StudentAcademicInfoUpdateRequest request) {
        studentValidationService.validateDepartment(request.department());
        studentValidationService.validateMajor(request.major());

        Student student = studentRepository.getById(userId);
        // 학번에 변경 사항이 생겼을 경우
        String oldStudentNumber = student.getStudentNumber();
        String newStudentNumber = request.studentNumber();
        boolean updateStudentNumber = isChangeStudentNumber(newStudentNumber, oldStudentNumber);
        // 학부에 변경 사항이 생겼을 경우
        Department newDepartment = departmentRepository.getByName(request.department());
        // 전공에 변경 사항이 생겼을 경우
        Major newMajor = majorRepository.getByNameAndDepartmentId(request.major(), student.getDepartment().getId());
        Major oldMajor = student.getMajor();
        boolean updateMajor = isChangedMajor(oldMajor, newMajor);

        student.updateStudentAcademicInfo(newStudentNumber, newDepartment, newMajor);

        /**
         * 해당 API에서는 Major를 수정할 수 있음 (여기서 그대로는 null이 아닌 경우)
         * 1. 학번, 전공 모두 변경
         * 2. 전공만 변경 (학번, 학부는 그대로)
         * 3. 학번만 변경 (학부, 전공은 그대로)
         */
        if (updateStudentNumber && updateMajor) {
            graduationService.resetStudentCourseCalculation(student, newMajor);
        }
        else if (updateMajor) {
            if (student.getDepartment() != null && student.getStudentNumber() != null) {
                graduationService.resetStudentCourseCalculation(student, newMajor);
            }
        }
        else if (updateStudentNumber) {
            if (student.getDepartment() != null && student.getMajor() != null) {
                graduationService.resetStudentCourseCalculation(student, newMajor);
            }
        }

        return StudentAcademicInfoUpdateResponse.from(student);
    }

    private boolean isChangedMajor(Major oldMajor, Major newMajor) {
        return newMajor != null && !newMajor.equals(oldMajor);
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
        Department department = null;
        if (studentTemporaryStatus.get().getDepartment() != null) {
            department = departmentRepository.getByName(studentTemporaryStatus.get().getDepartment());
        }
        Student student = studentTemporaryStatus.get().toStudent(passwordEncoder, department);
        studentRepository.save(student);
        userRepository.save(student.getUser());
        studentRedisRepository.deleteById(student.getUser().getEmail());
        eventPublisher.publishEvent(new StudentRegisterEvent(student.getUser().getEmail()));
        return new ModelAndView("success_register_config");
    }

    @Transactional
    public void findPassword(FindPasswordRequest request, String serverURL) {
        User user = userRepository.getByEmail(request.email());
        String resetToken = UUID.randomUUID().toString();
        passwordResetTokenRepository.save(PasswordResetToken.of(resetToken, user.getId()));
        mailService.sendMail(request.email(), new StudentPasswordChangeData(serverURL, resetToken));
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
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.getByResetToken(resetToken);
        User authedUser = userRepository.getById(passwordResetToken.getId());
        authedUser.updatePassword(passwordEncoder, request.password());
        passwordResetTokenRepository.deleteById(passwordResetToken.getId());
    }

    public ModelAndView checkResetToken(String resetToken, String serverUrl) {
        ModelAndView modelAndView = new ModelAndView("change_password_config");
        modelAndView.addObject("contextPath", serverUrl);
        modelAndView.addObject("resetToken", resetToken);
        return modelAndView;
    }
}
