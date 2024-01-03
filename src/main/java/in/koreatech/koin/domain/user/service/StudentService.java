package in.koreatech.koin.domain.user.service;

import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.domain.user.model.Student;
import in.koreatech.koin.domain.user.dto.StudentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentService {

    public StudentResponse getStudent(Student student) {
        if (student == null || student.getId() == null) {
            throw new UserNotFoundException("학생 정보가 비어있습니다.");
        }
        return StudentResponse.from(student);
    }
}
