package in.koreatech.koin.domain.student.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

import in.koreatech.koin._common.auth.JwtProvider;
import in.koreatech.koin._common.concurrent.ConcurrencyGuard;
import in.koreatech.koin._common.event.StudentFindPasswordEvent;
import in.koreatech.koin._common.event.StudentRegisterEvent;
import in.koreatech.koin._common.event.StudentRegisterRequestEvent;
import in.koreatech.koin._common.event.UserRegisterEvent;
import in.koreatech.koin.admin.abtest.useragent.UserAgentInfo;
import in.koreatech.koin.domain.graduation.repository.StandardGraduationRequirementsRepository;
import in.koreatech.koin.domain.graduation.service.GraduationService;
import in.koreatech.koin.domain.student.dto.RegisterStudentRequest;
import in.koreatech.koin.domain.student.dto.RegisterStudentRequestV2;
import in.koreatech.koin.domain.student.dto.StudentLoginRequest;
import in.koreatech.koin.domain.student.dto.StudentLoginResponse;
import in.koreatech.koin.domain.student.dto.StudentResponse;
import in.koreatech.koin.domain.student.dto.StudentWithAcademicResponse;
import in.koreatech.koin.domain.student.dto.UpdateStudentAcademicInfoRequest;
import in.koreatech.koin.domain.student.dto.UpdateStudentAcademicInfoResponse;
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
import in.koreatech.koin.domain.student.util.StudentUtil;
import in.koreatech.koin.domain.timetableV3.exception.ChangeMajorNotExistException;
import in.koreatech.koin.domain.user.dto.UserAuthTokenRequest;
import in.koreatech.koin.domain.user.dto.UserChangePasswordRequest;
import in.koreatech.koin.domain.user.dto.UserChangePasswordSubmitRequest;
import in.koreatech.koin.domain.user.dto.UserFindPasswordRequest;
import in.koreatech.koin.domain.user.model.PasswordResetToken;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.domain.user.repository.UserPasswordResetTokenRedisRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.service.RefreshTokenService;
import in.koreatech.koin.domain.user.service.UserService;
import in.koreatech.koin.domain.user.service.UserValidationService;
import in.koreatech.koin.domain.user.verification.service.UserVerificationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserVerificationService userVerificationService;
    private final UserValidationService userValidationService;
    private final StudentValidationService studentValidationService;
    private final RefreshTokenService refreshTokenService;
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

    @Transactional
    public void studentRegister(RegisterStudentRequest request, String serverURL) {
        studentValidationService.validateStudentRegister(request);
        String authToken = UUID.randomUUID().toString();

        UnAuthenticatedStudentInfo unauthenticatedStudentInfo = UnAuthenticatedStudentInfo.of(request, authToken);
        studentRedisRepository.save(unauthenticatedStudentInfo);

        eventPublisher.publishEvent(new StudentRegisterRequestEvent(request.email(), serverURL, authToken));
    }

    @Transactional
    public StudentLoginResponse studentLogin(StudentLoginRequest request, UserAgentInfo userAgentInfo) {
        User user = userService.getByEmailAndUserTypeIn(request.email(), UserType.KOIN_STUDENT_TYPES);
        user.requireSameLoginPw(passwordEncoder, request.password());
        userValidationService.checkUserAuthentication(request.email());

        String accessToken = jwtProvider.createToken(user);
        String refreshToken = refreshTokenService.createRefreshToken(user.getId(), userAgentInfo.getType());
        user.updateLastLoggedTime(LocalDateTime.now());

        return StudentLoginResponse.of(accessToken, refreshToken);
    }

    @Transactional
    public UpdateStudentResponse updateStudent(Integer userId, UpdateStudentRequest request) {
        Student student = studentRepository.getById(userId);
        User user = student.getUser();
        userValidationService.requireUniqueNicknameUpdate(request.nickname(), user);

        updateStudentInfo(student, request.studentNumber(), request.major());
        user.update(user.getEmail(), request.nickname(), request.name(), request.phoneNumber(), request.gender());
        user.updatePassword(passwordEncoder, request.password());

        return UpdateStudentResponse.from(student);
    }

    @Transactional
    public UpdateStudentResponse updateStudentV2(Integer userId, UpdateStudentRequestV2 request) {
        Student student = studentRepository.getById(userId);
        User user = student.getUser();

        userValidationService.requireUniqueUpdate(
            request.nickname(),
            request.phoneNumber(),
            request.email(),
            user
        );

        updateStudentInfo(student, request.studentNumber(), request.major());
        user.update(request.email(), request.nickname(), request.name(), request.phoneNumber(), request.gender());
        user.updatePassword(passwordEncoder, request.password());

        return UpdateStudentResponse.from(student);
    }

    private void updateStudentInfo(Student student, String studentNumber, String major) {
        boolean isUpdatedStudentNumber = updateStudentNumber(student, studentNumber);
        boolean isUpdatedDepartment = updateDepartment(student, major);
        updateCourseCalculation(student, isUpdatedStudentNumber, isUpdatedDepartment);
    }

    private boolean updateStudentNumber(Student student, String newStudentNumber) {
        if (student.isNotSameStudentNumber(newStudentNumber)) {
            student.updateStudentNumber(newStudentNumber);
            return true;
        }
        return false;
    }

    private boolean updateDepartment(Student student, String major) {
        studentValidationService.validateDepartment(major);
        Department department = departmentRepository.findByName(major).orElse(null);
        boolean isUpdatedDepartment = student.isNotSameDepartment(department);
        /**
         * 해당 API에서는 학생의 Major를 수정할 수 없음.
         * 졸업학점계산기 설계 상으로 학번, 학부, 전공이 변경되면 졸업학점 관련 메소드를 호출해야함.
         * Department로 조회된 Major의 첫 번째 값을 Student의 Major으로 설정
         * 단, Department가 변경될 경우만 설정
         * 초기 major 설정이 안 되어 있는 경우에도 동일한 로직을 수행
         */
        Major newMajor;
        if (department != null && isUpdatedDepartment) {
            List<Major> majors = majorRepository.findByDepartmentId(department.getId());
            newMajor = majors.get(0);
            student.updateDepartmentMajor(department, newMajor);
        } else if (department == null) {
            student.updateDepartmentMajor(null, null);
        }
        return isUpdatedDepartment;
    }

    private void updateCourseCalculation(Student student, boolean isUpdatedStudentNumber, boolean isUpdatedDepartment) {
        /**
         * 1. 학생의 학변이 변경됐고, 학부 변경이 없는경우 (학부가 있냐 / 학부가 없냐)
         * 2. 학생의 학번이 변경이 안되고, 학부 변경이 있는 경우 (학번이 있냐 / 학번이 없냐)
         * 3. 학생의 학번도 변경되고, 학부 변경도 있는 경우
         */
        if ((isUpdatedStudentNumber || isUpdatedDepartment)
            && student.getStudentNumber() != null
            && student.getDepartment() != null
        ) {
            graduationService.resetStudentCourseCalculation(student, student.getMajor());
        }
    }

    @Transactional
    public UpdateStudentAcademicInfoResponse updateStudentAcademicInfo(
        Integer userId, UpdateStudentAcademicInfoRequest request
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
            updateStudentNumber = student.isNotSameStudentNumber(requestStudentNumber);
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

        return UpdateStudentAcademicInfoResponse.from(student);
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
    public ModelAndView authenticate(UserAuthTokenRequest request) {
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
    public void studentRegisterV2(RegisterStudentRequestV2 request) {
        studentValidationService.validateDepartment(request.department());
        userValidationService.requireUniqueRegister(
            request.nickname(),
            request.phoneNumber(),
            request.email(),
            request.loginId()
        );
        Department department = departmentRepository.getByName(request.department());
        Student student = request.toStudent(passwordEncoder, department);
        studentRepository.save(student);
        userRepository.save(student.getUser());
        eventPublisher.publishEvent(
            new UserRegisterEvent(student.getUser().getId(), request.marketingNotificationAgreement())
        );
        userVerificationService.consumeVerification(request.phoneNumber());
    }

    @Transactional
    public void findPassword(UserFindPasswordRequest request, String serverURL) {
        User user = userService.getByEmailAndUserTypeIn(request.email(), UserType.KOIN_STUDENT_TYPES);
        String resetToken = UUID.randomUUID().toString();
        passwordResetTokenRepository.save(PasswordResetToken.of(resetToken, user.getId()));
        eventPublisher.publishEvent(new StudentFindPasswordEvent(request.email(), serverURL, resetToken));
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
    public void changePassword(Integer userId, UserChangePasswordRequest request) {
        Student student = studentRepository.getById(userId);
        User user = student.getUser();
        user.updatePassword(passwordEncoder, request.password());
        refreshTokenService.deleteAllRefreshTokens(user.getId());
    }

    @Transactional
    public void changePasswordSubmit(UserChangePasswordSubmitRequest request, String resetToken) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.getByResetToken(resetToken);
        User authedUser = userService.getById(passwordResetToken.getId());
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
