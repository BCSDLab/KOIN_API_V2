package in.koreatech.koin.domain.graduation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.graduation.model.StudentCourseCalculation;

public interface StudentCourseCalculationRepository extends Repository<StudentCourseCalculation, Integer> {

    void save(StudentCourseCalculation studentCourseCalculation);

    Optional<StudentCourseCalculation> findByUserId(Integer userId);

    List<StudentCourseCalculation> findAllByUserId(Integer userId);

    StudentCourseCalculation findByUserIdAndStandardGraduationRequirementsId(Integer userId, Integer id);

    void delete(StudentCourseCalculation existingCalculation);

    void deleteAllByUserId(Integer userId);
}
