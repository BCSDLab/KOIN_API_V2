package in.koreatech.koin.domain.graduation.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.graduation.model.StudentCourseCalculation;

public interface StudentCourseCalculationRepository extends Repository<StudentCourseCalculation, Integer> {

    Optional<StudentCourseCalculation> findByUserId(Integer userId);

    void deleteAllByUserId(Integer userId);

    void save(StudentCourseCalculation studentCourseCalculation);
}
