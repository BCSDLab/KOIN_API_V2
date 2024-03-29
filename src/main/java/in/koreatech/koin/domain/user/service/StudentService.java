package in.koreatech.koin.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.user.dto.StudentResponse;
import in.koreatech.koin.domain.user.dto.StudentUpdateRequest;
import in.koreatech.koin.domain.user.dto.StudentUpdateResponse;
import in.koreatech.koin.domain.user.exception.DuplicationNicknameException;
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
    public StudentUpdateResponse updateStudent(Long userId, StudentUpdateRequest request) {
        Student student = studentRepository.getById(userId);
        User user = student.getUser();
        CheckNicknameDuplication(request.nickname());
        user.update(request.nickname(), request.name(),
            request.phoneNumber(), UserGender.from(request.gender()));
        student.update(request.studentNumber(),
            StudentDepartment.from(request.major()));
        studentRepository.save(student);

        return StudentUpdateResponse.from(student);
    }

    public void CheckNicknameDuplication(String nickname) {
        if (nickname != null && userRepository.existsByNickname(nickname)) {
            throw DuplicationNicknameException.withDetail("nickname : " + nickname);
        }
    }
}

