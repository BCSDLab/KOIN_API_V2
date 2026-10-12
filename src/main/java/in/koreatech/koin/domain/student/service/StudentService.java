package in.koreatech.koin.domain.student.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import in.koreatech.koin.domain.student.dto.UpdateStudentAcademicInfoRequest;
import in.koreatech.koin.domain.student.dto.UpdateStudentAcademicInfoResponse;
import in.koreatech.koin.domain.student.dto.UpdateStudentRequest;
import in.koreatech.koin.domain.student.dto.UpdateStudentRequestV2;
import in.koreatech.koin.domain.student.dto.UpdateStudentResponse;
import in.koreatech.koin.domain.student.exception.MajorNotFoundException;
import in.koreatech.koin.domain.student.exception.StudentDepartmentNotValidException;
import in.koreatech.koin.domain.student.model.AcademicProgramYear;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.model.redis.UnAuthenticatedStudentInfo;
import in.koreatech.koin.domain.student.repository.AcademicProgramYearRepository;
import in.koreatech.koin.domain.student.repository.DepartmentRepository;
import in.koreatech.koin.domain.student.repository.MajorRepository;
import in.koreatech.koin.domain.student.repository.StudentRedisRepository;
import in.koreatech.koin.domain.student.repository.StudentRepository;
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
import in.koreatech.koin.global.auth.JwtProvider;
import in.koreatech.koin.global.concurrent.ConcurrencyGuard;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final UserService userService;
    private final UserVerificationService userVerificationService;
    private final UserValidationService userValidationService;
    private final StudentValidationService studentValidationService;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final StudentRepository studentRepository;
    private final StudentRedisRepository studentRedisRepository;
    private final JwtProvider jwtProvider;
    private final DepartmentRepository departmentRepository;
    private final MajorRepository majorRepository;
    private final AcademicProgramYearRepository academicProgramYearRepository;
    private final GraduationService graduationService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final UserPasswordResetTokenRedisRepository passwordResetTokenRepository;

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
        User user = userRepository.getByEmailAndUserTypeIn(request.email(), UserType.KOIN_STUDENT_TYPES);
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

        user.update(request.email(), request.nickname(), request.name(), request.phoneNumber(), request.gender());
        user.updatePassword(passwordEncoder, request.password());
        updateStudentInfo(student, request.studentNumber(), request.major());

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
        // 기존 프로필 API는 전공 선택을 받지 않는다. 학부 변경 시에만 기본 전공을 정한다.
        Major newMajor;
        if (department != null && isUpdatedDepartment) {
            newMajor = findDefaultMajor(student.getStudentNumber(), department);
            validateAcademicProgram(student.getStudentNumber(), department, newMajor);
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

        String newStudentNumber = student.getStudentNumber();
        String requestStudentNumber = request.studentNumber();
        if (requestStudentNumber != null) {
            newStudentNumber = requestStudentNumber;
        }

        Department newDepartment;
        if (request.department() != null) {
            newDepartment = departmentRepository.getByName(request.department());
        } else {
            newDepartment = null;
        }

        Major newMajor;
        if (request.major() != null) {
            if (newDepartment == null) {
                throw StudentDepartmentNotValidException.withDetail("전공 선택에는 학부가 필요합니다.");
            }
            newMajor = majorRepository.getByNameAndDepartmentId(request.major(), newDepartment.getId());
        } else if (newDepartment != null && Objects.equals(student.getDepartment(), newDepartment)) {
            newMajor = student.getMajor();
        } else if (newDepartment != null) {
            newMajor = findDefaultMajor(newStudentNumber, newDepartment);
        } else {
            newMajor = null;
        }

        boolean isAcademicInfoChanged = !Objects.equals(student.getStudentNumber(), newStudentNumber)
            || !Objects.equals(student.getMajor(), newMajor);
        if (!Objects.equals(student.getDepartment(), newDepartment)
            || !Objects.equals(student.getMajor(), newMajor)) {
            validateAcademicProgram(newStudentNumber, newDepartment, newMajor);
        }
        student.updateStudentAcademicInfo(newStudentNumber, newDepartment, newMajor);
        // 졸업 계산 갱신이 영속성 컨텍스트를 비우기 전에 응답의 학적 값을 읽는다.
        UpdateStudentAcademicInfoResponse response = UpdateStudentAcademicInfoResponse.from(student);

        if (isAcademicInfoChanged && newStudentNumber != null && newMajor != null) {
            graduationService.resetStudentCourseCalculation(student, newMajor);
        }

        return response;
    }

    private Major findDefaultMajor(String studentNumber, Department department) {
        List<AcademicProgramYear> programs = findAcademicPrograms(studentNumber);
        if (programs.isEmpty()) {
            return majorRepository.findFirstByDepartmentIdOrderByIdAsc(department.getId()).orElse(null);
        }
        return programs.stream()
            .filter(program -> program.getDepartment().getId().equals(department.getId()))
            .map(AcademicProgramYear::getMajor)
            .filter(Objects::nonNull)
            .filter(major -> major.getName() == null)
            .findFirst().orElse(null);
    }

    private List<AcademicProgramYear> findAcademicPrograms(String studentNumber) {
        if (studentNumber == null || studentNumber.length() < 4) {
            return List.of();
        }
        return academicProgramYearRepository
            .findAllByStudentYearOrderByDepartmentIdAscIdAsc(studentNumber.substring(0, 4));
    }

    private void validateAcademicProgram(String studentNumber, Department department, Major major) {
        if (department == null) {
            return;
        }
        List<AcademicProgramYear> programs = findAcademicPrograms(studentNumber);
        if (programs.isEmpty()) {
            return;
        }
        boolean validDepartment = programs.stream()
            .anyMatch(program -> program.getDepartment().getId().equals(department.getId()));
        if (!validDepartment) {
            throw StudentDepartmentNotValidException.withDetail(
                "학번 연도: " + studentNumber.substring(0, 4) + ", 학부: " + department.getName());
        }
        if (major != null && programs.stream().noneMatch(program -> program.getMajor() != null
            && program.getDepartment().getId().equals(department.getId())
            && program.getMajor().getId().equals(major.getId()))) {
            throw MajorNotFoundException.withDetail(
                "학번 연도: " + studentNumber.substring(0, 4) + ", 전공: " + major.getName());
        }
    }

    @Transactional
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
            major = findDefaultMajor(studentTemporaryStatus.get().getStudentNumber(), department);
            validateAcademicProgram(studentTemporaryStatus.get().getStudentNumber(), department, major);
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
        validateAcademicProgram(request.studentNumber(), department, null);
        Student student = request.toStudent(passwordEncoder, department);
        studentRepository.save(student);
        userRepository.save(student.getUser());
        if (request.marketingNotificationAgreement()) {
            eventPublisher.publishEvent(new UserMarketingAgreementEvent(student.getUser().getId()));
        }
        eventPublisher.publishEvent(new StudentRegisterEvent(student.getUser().getPhoneNumber()));
        userVerificationService.consumeVerification(request.phoneNumber());
    }

    @Transactional
    public void findPassword(UserFindPasswordRequest request, String serverURL) {
        User user = userRepository.getByEmailAndUserTypeIn(request.email(), UserType.KOIN_STUDENT_TYPES);
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
