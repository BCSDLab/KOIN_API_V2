package in.koreatech.koin.domain.student.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.domain.student.model.Student;

public interface StudentRepository extends Repository<Student, Integer> {

    Student save(Student student);

    Optional<Student> findById(Integer userId);

    default Student getById(Integer userId) {
        return findById(userId)
            .orElseThrow(() -> UserNotFoundException.withDetail("userId: " + userId));
    }

    void deleteByUserId(Integer userId);
}
