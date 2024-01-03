package in.koreatech.koin.domain.user.repository;

import in.koreatech.koin.domain.user.model.Student;
import java.util.Optional;
import org.springframework.data.repository.Repository;

public interface StudentRepository extends Repository<Student, Long> {

    Student save(Student student);

    Optional<Student> findById(Long id);

    Boolean existsById(Long id);

    void delete(Student student);
}
