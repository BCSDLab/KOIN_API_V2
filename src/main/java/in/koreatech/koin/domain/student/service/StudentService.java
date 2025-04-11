package in.koreatech.koin.domain.student.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import in.koreatech.koin._common.auth.JwtProvider;
import in.koreatech.koin._common.auth.exception.AuthenticationException;
import in.koreatech.koin._common.concurrent.ConcurrencyGuard;
import in.koreatech.koin._common.event.StudentEmailRequestEvent;
import in.koreatech.koin._common.event.StudentRegisterEvent;
import in.koreatech.koin.domain.graduation.repository.StandardGraduationRequirementsRepository;
import in.koreatech.koin.domain.graduation.service.GraduationService;
import in.koreatech.koin.domain.student.dto.StudentAcademicInfoUpdateRequest;
import in.koreatech.koin.domain.student.dto.StudentAcademicInfoUpdateResponse;
import in.koreatech.koin.domain.student.dto.StudentLoginRequest;
import in.koreatech.koin.domain.student.dto.StudentLoginResponse;
import in.koreatech.koin.domain.student.dto.StudentRegisterRequest;
import in.koreatech.koin.domain.student.dto.StudentRegisterRequestV2;
import in.koreatech.koin.domain.student.dto.StudentResponse;
import in.koreatech.koin.domain.student.dto.StudentUpdateRequest;
import in.koreatech.koin.domain.student.dto.StudentUpdateResponse;
import in.koreatech.koin.domain.student.dto.StudentWithAcademicResponse;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.model.redis.UnAuthenticatedStudentInfo;
import in.koreatech.koin.domain.student.repository.DepartmentRepository;
import in.koreatech.koin.domain.student.repository.MajorRepository;
import in.koreatech.koin.domain.student.repository.StudentRedisRepository;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.student.util.StudentUtil;
import in.koreatech.koin.domain.timetableV3.exception.ChangeMajorNotExistException;
import in.koreatech.koin.domain.user.dto.AuthTokenRequest;
import in.koreatech.koin.domain.user.dto.FindPasswordRequest;
import in.koreatech.koin.domain.user.dto.UserPasswordChangeRequest;
import in.koreatech.koin.domain.user.dto.UserPasswordChangeSubmitRequest;
import in.koreatech.koin.domain.user.model.PasswordResetToken;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserToken;
import in.koreatech.koin.domain.user.model.UserVerificationStatus;
import in.koreatech.koin.domain.user.repository.UserPasswordResetTokenRedisRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.repository.UserTokenRedisRepository;
import in.koreatech.koin.domain.user.repository.UserVerificationStatusRedisRepository;
import in.koreatech.koin.domain.user.service.RefreshTokenService;
import in.koreatech.koin.domain.user.service.UserService;
import in.koreatech.koin.domain.user.service.UserValidationService;
import in.koreatech.koin.integration.email.form.StudentPasswordChangeData;
import in.koreatech.koin.integration.email.form.StudentRegistrationData;
import in.koreatech.koin.integration.email.service.MailService;
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
    private final RefreshTokenService refreshTokenService;
    private final UserTokenRedisRepository userTokenRedisRepository;
    private final StudentRepository studentRepository;
    private final StudentRedisRepository studentRedisRepository;
    private final JwtProvider jwtProvider;
    private final DepartmentRepository departmentRepository;
    private final MajorRepository majorRepository;
    private final GraduationService graduationService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final UserPasswordResetTokenRedisRepository passwordResetTokenRepository;
    private final StandardGraduationRequirementsRepository standardGraduationRequirementsRepository;
    private final UserVerificationStatusRedisRepository userVerificationStatusRedisRepository;

    @Transactional
    public void studentRegister(StudentRegisterRequest request, String serverURL) {
        studentValidationService.validateStudentRegister(request);
        String authToken = UUID.randomUUID().toString();

        UnAuthenticatedStudentInfo unauthenticatedStudentInfo = UnAuthenticatedStudentInfo.of(request, authToken);
        studentRedisRepository.save(unauthenticatedStudentInfo);

        mailService.sendMail(request.email(), new StudentRegistrationData(serverURL, authToken));
        eventPublisher.publishEvent(new StudentEmailRequestEvent(request.email()));
    }

    @Transactional
    public StudentLoginResponse studentLogin(StudentLoginRequest request) {
        User user = userValidationService.checkLoginCredentials(request.email(), request.password());
        userValidationService.checkUserAuthentication(request.email());

        String accessToken = jwtProvider.createToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user);
        UserToken savedToken = userTokenRedisRepository.save(UserToken.create(user.getId(), refreshToken));
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
        Department newDepartment = departmentRepository.findByName(request.major()).orElse(null);
        Department oldDepartment = student.getDepartment();

        /**
         * 해당 API에서는 학생의 Major를 수정할 수 없음.
         * 졸업학점계산기 설계 상으로 학번, 학부, 전공이 변경되면 졸업학점 관련 메소드를 호출해야함.
         * Department로 조회된 Major의 첫 번째 값을 Student의 Major으로 설정
         * 단, Department가 변경될 경우만 설정
         * 초기 major 설정이 안 되어 있는 경우에도 동일한 로직을 수행
         */
        Major newMajor = null;
        boolean updateDepartment = isChangedDepartment(oldDepartment, newDepartment);
        if (updateDepartment && newDepartment != null) {
            List<Major> majors = majorRepository.findByDepartmentId(newDepartment.getId());
            newMajor = majors.get(0);
            student.updateDepartmentMajor(newDepartment, newMajor);
        } else if (newDepartment == null) {
            student.updateDepartmentMajor(null, null);
        }

        /**
         * 1. 학생의 학변이 변경됐고, 학부 변경이 없는경우 (학부가 있냐 / 학부가 없냐)
         * 2. 학생의 학번이 변경이 안되고, 학부 변경이 있는 경우 (학번이 있냐 / 학번이 없냐)
         * 3. 학생의 학번도 변경되고, 학부 변경도 있는 경우
         */
        if ((updateStudentNumber || updateDepartment)
            && student.getStudentNumber() != null
            && student.getDepartment() != null
        ) {
            graduationService.resetStudentCourseCalculation(student, newMajor);
        }

        user.update(request.nickname(), request.name(), request.phoneNumber(), request.gender());
        user.updateStudentPassword(passwordEncoder, request.password());

        return StudentUpdateResponse.from(student);
    }

    private boolean isChangeStudentNumber(String newStudentNumber, String oldStudentNumber) {
        return !Objects.equals(newStudentNumber, oldStudentNumber);
    }

    private boolean isChangedDepartment(Department oldDepartment, Department newDepartment) {
        return !Objects.equals(newDepartment, oldDepartment);
    }

    @Transactional
    public StudentAcademicInfoUpdateResponse updateStudentAcademicInfo(
        Integer userId, StudentAcademicInfoUpdateRequest request
    ) {
        studentValidationService.validateDepartment(request.department());
        studentValidationService.validateMajor(request.major());

        Student student = studentRepository.getById(userId);

        String oldStudentNumber = student.getStudentNumber();
        String newStudentNumber = student.getStudentNumber();
        String requestStudentNumber = request.studentNumber();
        if (requestStudentNumber != null) {
            newStudentNumber = requestStudentNumber;
        }

        // 학번 변경 사항 감지
        boolean updateStudentNumber = false;
        if (requestStudentNumber != null && oldStudentNumber != null) {
            updateStudentNumber = isChangeStudentNumber(requestStudentNumber, oldStudentNumber);
        }

        Department newDepartment;
        if (request.department() != null) {
            newDepartment = departmentRepository.getByName(request.department());
        } else {
            newDepartment = null;
        }

        Major oldMajor = student.getMajor();
        Major newMajor;
        if (request.major() != null) {
            newMajor = majorRepository.getByNameAndDepartmentId(request.major(), newDepartment.getId());
        } else if (newDepartment != null) {
            newMajor = majorRepository.findFirstByDepartmentIdOrderByIdAsc(newDepartment.getId()).orElse(null);
        } else {
            newMajor = null;
        }

        validateMajorChange(newStudentNumber, newMajor);

        // 전공 변경 사항 감지
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
        } else if (updateMajor) {
            if (student.getDepartment() != null && student.getStudentNumber() != null) {
                graduationService.resetStudentCourseCalculation(student, newMajor);
            }
        } else if (updateStudentNumber) {
            if (student.getDepartment() != null && student.getMajor() != null) {
                graduationService.resetStudentCourseCalculation(student, newMajor);
            }
        }

        return StudentAcademicInfoUpdateResponse.from(student);
    }

    private void validateMajorChange(String studentNumber, Major newMajor) {
        if (newMajor == null) {
            return;
        }

        String studentYear = StudentUtil.parseStudentNumberYearAsString(studentNumber);

        boolean exists = standardGraduationRequirementsRepository.existsByMajorIdAndYear(
            newMajor.getId(), studentYear
        );

        if (!exists) {
            throw ChangeMajorNotExistException.withDetail("studentYear: " + studentYear + " major: " + newMajor);
        }
    }

    private boolean isChangedMajor(Major oldMajor, Major newMajor) {
        return newMajor != null && !newMajor.equals(oldMajor);
    }

    @ConcurrencyGuard(lockName = "studentAuthenticate")
    public ModelAndView authenticate(AuthTokenRequest request) {
        Optional<UnAuthenticatedStudentInfo> studentTemporaryStatus = studentRedisRepository.findByAuthToken(
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
        Major major = null;
        if (department != null) {
            major = majorRepository.findByDepartmentId(department.getId()).get(0);
        }
        Student student = studentTemporaryStatus.get().toStudent(passwordEncoder, department, major);
        studentRepository.save(student);
        userRepository.save(student.getUser());
        studentRedisRepository.deleteById(student.getUser().getEmail());
        eventPublisher.publishEvent(new StudentRegisterEvent(student.getUser().getEmail()));
        return new ModelAndView("success_register_config");
    }

    @Transactional
    public void studentRegisterV2(StudentRegisterRequestV2 request) {
        checkVerified(request.phoneNumber());
        Student student = request.toStudent(passwordEncoder);
        studentRepository.save(student);
        userRepository.save(student.getUser());
    }

    private void checkVerified(String phoneNumber) {
        userVerificationStatusRedisRepository.findById(phoneNumber)
            .filter(UserVerificationStatus::isVerified)
            .orElseThrow(() -> new AuthenticationException("본인 인증 후 다시 시도해주십시오."));
        userVerificationStatusRedisRepository.deleteById(phoneNumber);
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

    public StudentWithAcademicResponse getStudentWithAcademicInfo(Integer userId) {
        Student student = studentRepository.getById(userId);
        return StudentWithAcademicResponse.from(student);
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
