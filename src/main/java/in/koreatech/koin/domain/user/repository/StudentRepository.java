package in.koreatech.koin.domain.user.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.domain.user.model.Student;

public interface StudentRepository extends Repository<Student, Long> {

    Student save(Student student);

    Optional<Student> findById(Long id);

    default Student getById(Long id) {
        return findById(id).orElseThrow(() -> UserNotFoundException.withDetail("id" + id));
    }
}
