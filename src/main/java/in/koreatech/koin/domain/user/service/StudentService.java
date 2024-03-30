package in.koreatech.koin.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.user.dto.StudentRegisterRequest;
import in.koreatech.koin.domain.user.dto.StudentResponse;
import in.koreatech.koin.domain.user.dto.StudentUpdateRequest;
import in.koreatech.koin.domain.user.dto.StudentUpdateResponse;
import in.koreatech.koin.domain.user.exception.DuplicationNicknameException;
import in.koreatech.koin.domain.user.exception.StudentDepartmentNotValidException;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.StudentDepartment;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.repository.StudentRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.domain.email.exception.DuplicationEmailException;
import in.koreatech.koin.global.domain.email.model.EmailAddress;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

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

    public void StudentRegister(StudentRegisterRequest request) {
        Student student = StudentRegisterRequest.toStudent(request);
        validateInRegister(student);
    }

    private void validateInRegister(Student student) {
        EmailAddress emailAddress = EmailAddress.from(student.getUser().getEmail());
        emailAddress.validatePortalEmail();

        validateUniqueness(student);
        validateStudentNumber(student);
    }

    private void validateUniqueness(Student student) {
        validateEmailUniqueness(student.getUser().getEmail());
        validateNicknameUniqueness(student.getUser().getNickname());
    }

    private void validateEmailUniqueness(String email) {
        userRepository.findByEmail(email)
            .ifPresent(user -> {
                throw DuplicationEmailException.withDetail("email: " + email);
            });
    }

    private void validateNicknameUniqueness(String nickname) {
        userRepository.findByEmail(nickname)
            .ifPresent(user -> {
                throw DuplicationNicknameException.withDetail("nickname: " + nickname);
            });
    }

    private void validateStudentNumber(Student student) {
        if (student.getStudentNumber() == null) {
            return;
        }

    }
}

