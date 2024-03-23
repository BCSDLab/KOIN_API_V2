package in.koreatech.koin.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.user.dto.StudentResponse;
import in.koreatech.koin.domain.user.dto.StudentUpdateRequest;
import in.koreatech.koin.domain.user.dto.StudentUpdateResponse;
import in.koreatech.koin.domain.user.exception.DuplicationNicknameException;
import in.koreatech.koin.domain.user.exception.StudentNumberNotValidException;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.model.StudentDepartment;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.model.UserGender;
import in.koreatech.koin.domain.user.repository.StudentRepository;
import in.koreatech.koin.domain.user.repository.UserRepository;
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
    public StudentUpdateResponse updateStudent(Long userId, StudentUpdateRequest studentUpdateRequest) {
        Student student = studentRepository.getById(userId);
        User user = student.getUser();

        if (studentUpdateRequest.nickname() != null &&
            userRepository.existsByNickname(studentUpdateRequest.nickname())) {
            throw new DuplicationNicknameException("이미 존재하는 닉네임입니다. nickname : " + studentUpdateRequest.nickname());
        }

        if (studentUpdateRequest.studentNumber() != null &&
            !Student.isValidStudentNumber(studentUpdateRequest.studentNumber())) {
            throw new StudentNumberNotValidException(
                "학생의 학번 형식이 아닙니다. studentNumber : " + studentUpdateRequest.studentNumber());
        }

        User updateUser = User.builder()
            .gender(UserGender.from(studentUpdateRequest.gender()))
            .name(studentUpdateRequest.name())
            .nickname(studentUpdateRequest.nickname())
            .phoneNumber(studentUpdateRequest.phoneNumber())
            .build();

        Student updateStudent = Student.builder()
            .department(StudentDepartment.from(studentUpdateRequest.major()))
            .studentNumber(studentUpdateRequest.studentNumber())
            .build();

        user.update(updateUser);
        student.update(updateStudent);

        studentRepository.save(student);

        return StudentUpdateResponse.from(student);
    }
}

