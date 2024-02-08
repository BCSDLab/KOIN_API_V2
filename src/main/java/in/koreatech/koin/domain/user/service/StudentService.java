package in.koreatech.koin.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.user.dto.StudentResponse;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.repository.StudentRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentResponse getStudent(Long userId) {
        Student student = studentRepository.getById(userId);
        return StudentResponse.from(student);
    }
}
