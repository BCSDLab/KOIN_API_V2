package in.koreatech.koin.domain.collegecredit.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.collegecredit.model.StudentCourseCalculation;

public interface StudentCourseCalculationRepository extends Repository<StudentCourseCalculation, Integer> {
    Optional<StudentCourseCalculation> findByUserIdAndStandardGraduationRequirementsId(Integer userId,
        Integer standardGraduationRequirementsId);

    List<StudentCourseCalculation> findAllByUserId(Integer userId);

    StudentCourseCalculation save(StudentCourseCalculation studentCourseCalculation);
}
