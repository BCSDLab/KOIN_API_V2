package in.koreatech.koin.domain.student.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin._common.exception.CustomException;
import in.koreatech.koin._common.exception.errorcode.ErrorCode;
import in.koreatech.koin.domain.student.model.Student;

public interface StudentRepository extends Repository<Student, Integer> {

    Student save(Student student);

    Optional<Student> findById(Integer userId);

    default Student getById(Integer userId) {
        return findById(userId)
            .orElseThrow(() -> CustomException.of(ErrorCode.NOT_FOUND_USER, "userId: " + userId));
    }

    void deleteByUserId(Integer userId);
}
