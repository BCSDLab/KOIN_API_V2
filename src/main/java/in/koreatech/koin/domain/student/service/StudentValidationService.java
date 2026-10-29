package in.koreatech.koin.domain.student.service;

import java.util.List;
import java.util.Objects;

import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Service;

import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.domain.student.dto.RegisterStudentRequest;
import in.koreatech.koin.domain.student.exception.MajorNotFoundException;
import in.koreatech.koin.domain.student.exception.StudentDepartmentNotValidException;
import in.koreatech.koin.domain.student.exception.StudentNumberNotValidException;
import in.koreatech.koin.domain.student.model.AcademicProgramYear;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.repository.AcademicProgramYearRepository;
import in.koreatech.koin.domain.student.repository.DepartmentRepository;
import in.koreatech.koin.domain.student.repository.MajorRepository;
import in.koreatech.koin.domain.student.repository.StudentRedisRepository;
import in.koreatech.koin.domain.student.util.StudentUtil;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.infrastructure.email.exception.DuplicationEmailException;
import in.koreatech.koin.infrastructure.email.model.EmailAddress;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentValidationService {

    private final UserRepository userRepository;
    private final StudentRedisRepository studentRedisRepository;
    private final MajorRepository majorRepository;
    private final DepartmentRepository departmentRepository;
    private final AcademicProgramYearRepository academicProgramYearRepository;

    private static final int MIN_YEAR = 1992;

    public void validateStudentRegister(RegisterStudentRequest request) {
        EmailAddress emailAddress = EmailAddress.from(request.email());
        emailAddress.validateKoreatechEmail();

        validateDataExist(request);
        validateStudentNumber(request.studentNumber());
        validateDepartment(request.major());
        validateAcademicProgram(request.studentNumber(), departmentRepository.getByName(request.major()), null);
    }

    public void validateDepartment(String department) {
        if (department == null || department.isBlank() || !departmentRepository.existsByName(department)) {
            throw StudentDepartmentNotValidException.withDetail("학부(학과) : " + department);
        }
    }

    public void validateMajor(String major) {
        if (major != null && !majorRepository.existsByName(major)) {
            throw MajorNotFoundException.withDetail("전공 : " + major);
        }
    }

    public void validateAcademicProgram(String studentNumber, Department department, Major major) {
        validateStudentNumberFormat(studentNumber);
        if (department == null) {
            throw StudentDepartmentNotValidException.withDetail("학부가 필요합니다.");
        }
        if (major != null && (major.getDepartment() == null
            || !Objects.equals(major.getDepartment().getId(), department.getId()))) {
            throw MajorNotFoundException.withDetail("학부: " + department.getName() + ", 전공: " + major.getName());
        }
        String studentYear = studentNumber.substring(0, 4);
        List<AcademicProgramYear> programs = academicProgramYearRepository
            .findAllByStudentYearOrderByDepartmentIdAscIdAsc(studentYear);
        if (programs.isEmpty()) {
            return;
        }
        boolean validDepartment = programs.stream()
            .anyMatch(program -> program.getDepartment().getId().equals(department.getId()));
        if (!validDepartment) {
            throw StudentDepartmentNotValidException.withDetail(
                "학번 연도: " + studentYear + ", 학부: " + department.getName());
        }
        if (major != null && programs.stream().noneMatch(program -> program.getMajor() != null
            && program.getDepartment().getId().equals(department.getId())
            && program.getMajor().getId().equals(major.getId()))) {
            throw MajorNotFoundException.withDetail(
                "학번 연도: " + studentYear + ", 전공: " + major.getName());
        }
    }

    public void validateDataExist(RegisterStudentRequest request) {
        validateEmailExist(request.email());
        validateNicknameExist(request.nickname());
    }

    public void validateEmailExist(String email) {
        userRepository.findByEmail(email)
            .ifPresent(user -> {
                throw DuplicationEmailException.withDetail("email: " + email);
            });
        studentRedisRepository.findById(email)
            .ifPresent(status -> {
                throw DuplicationEmailException.withDetail("email: " + email);
            });
    }

    public void validateNicknameExist(String nickname) {
        if (nickname == null) {
            return;
        }
        userRepository.findByNickname(nickname)
            .ifPresent(user -> {
                throw CustomException.of(ApiResponseCode.DUPLICATE_NICKNAME, "nickname : " + nickname);
            });
        studentRedisRepository.findByNickname(nickname)
            .ifPresent(status -> {
                throw CustomException.of(ApiResponseCode.DUPLICATE_NICKNAME, "nickname : " + nickname);
            });
    }

    public void validateStudentNumberFormat(String studentNumber) {
        if (studentNumber == null || !studentNumber.matches("[0-9]{8,10}")) {
            throw StudentNumberNotValidException.withDetail("studentNumber: " + studentNumber);
        }
    }

    public void validateStudentNumber(String studentNumber) {
        validateStudentNumberFormat(studentNumber);
        int studentNumberYear = StudentUtil.parseStudentNumberYear(studentNumber);
        if (studentNumberYear < MIN_YEAR || LocalDateTime.now().getYear() < studentNumberYear) {
            throw StudentNumberNotValidException.withDetail("studentNumber: " + studentNumber);
        }
    }
}
