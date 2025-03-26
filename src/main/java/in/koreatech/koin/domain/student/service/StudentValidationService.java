package in.koreatech.koin.domain.student.service;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.joda.time.LocalDateTime;
import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.owner.exception.DuplicationPhoneNumberException;
import in.koreatech.koin.domain.student.dto.StudentRegisterRequest;
import in.koreatech.koin.domain.student.exception.MajorNotFoundException;
import in.koreatech.koin.domain.student.exception.StudentDepartmentNotValidException;
import in.koreatech.koin.domain.student.exception.StudentNumberNotValidException;
import in.koreatech.koin.domain.student.model.StudentDepartment;
import in.koreatech.koin.domain.student.repository.MajorRepository;
import in.koreatech.koin.domain.student.repository.StudentRedisRepository;
import in.koreatech.koin.domain.student.util.StudentUtil;
import in.koreatech.koin.domain.user.exception.DuplicationNicknameException;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.integration.email.exception.DuplicationEmailException;
import in.koreatech.koin.integration.email.model.EmailAddress;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentValidationService {

    private final UserRepository userRepository;
    private final StudentRedisRepository studentRedisRepository;
    private final MajorRepository majorRepository;

    private static final int MIN_YEAR = 1992;

    public void validateStudentRegister(StudentRegisterRequest request) {
        EmailAddress emailAddress = EmailAddress.from(request.email());
        emailAddress.validateKoreatechEmail();

        validateDataExist(request);
        validateStudentNumber(request.studentNumber());
        validateDepartment(request.major());
    }

    public void validateUpdateNickname(String nickname, Integer userId) {
        User checkUser = userRepository.getById(userId);
        if (nickname != null && !nickname.equals(checkUser.getNickname())
            && userRepository.existsByNickname(nickname)) {
            throw DuplicationNicknameException.withDetail("nickname : " + nickname);
        }
    }

    public void validateDepartment(String department) {
        if (department != null && !StudentDepartment.isValid(department)) {
            throw StudentDepartmentNotValidException.withDetail("학부(학과) : " + department);
        }
    }

    public void validateMajor(String major) {
        if (major != null && !majorRepository.existsByName(major)) {
            throw MajorNotFoundException.withDetail("전공 : " + major);
        }
    }

    public void validateDataExist(StudentRegisterRequest request) {
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
                throw DuplicationNicknameException.withDetail("nickname: " + nickname);
            });
        studentRedisRepository.findByNickname(nickname)
            .ifPresent(status -> {
                throw DuplicationNicknameException.withDetail("nickname: " + nickname);
            });
    }

    public void validateStudentNumber(String studentNumber) {
        if (studentNumber == null) {
            return;
        }
        int studentNumberYear = StudentUtil.parseStudentNumberYear(studentNumber);
        if (studentNumberYear < MIN_YEAR || LocalDateTime.now().getYear() < studentNumberYear) {
            throw StudentNumberNotValidException.withDetail("studentNumber: " + studentNumber);
        }
    }

    public void validateExistPhoneNumber(String phoneNumber) {
        if (userRepository.findByPhoneNumberAndUserType(phoneNumber, STUDENT).isPresent()) {
            throw DuplicationPhoneNumberException.withDetail("account: " + phoneNumber);
        }
    }
}
