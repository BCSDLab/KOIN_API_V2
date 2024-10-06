package in.koreatech.koin.domain.graduation.repository;

import in.koreatech.koin.domain.graduation.model.StudentCourseCalculation;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface StudentCourseCalculationRepository extends Repository<StudentCourseCalculationRepository, Integer> {

    Optional<StudentCourseCalculation> findByUserId(Integer userId);

    void deleteAllByUserId(Integer userId);

    void save(StudentCourseCalculation studentCourseCalculation);
}
