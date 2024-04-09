package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.domain.user.model.Student;

public interface StudentRepository extends Repository<Student, Long> {

    Student save(Student student);

    Optional<Student> findById(Long userId);

    default Student getById(Long userId) {
        return findById(userId)
            .orElseThrow(() -> UserNotFoundException.withDetail("userId: " + userId));
    }

    void deleteByUserId(Long userId);
}
