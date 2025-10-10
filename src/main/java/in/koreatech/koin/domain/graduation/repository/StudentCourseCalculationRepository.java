package in.koreatech.koin.domain.graduation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.graduation.model.StandardGraduationRequirements;
import in.koreatech.koin.domain.graduation.model.StudentCourseCalculation;
import in.koreatech.koin.global.marker.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface StudentCourseCalculationRepository extends Repository<StudentCourseCalculation, Integer> {

    void save(StudentCourseCalculation studentCourseCalculation);

    List<StudentCourseCalculation> findAllByUserId(Integer userId);

    void delete(StudentCourseCalculation existingCalculation);

    void deleteAllByUserId(Integer userId);

    Optional<StudentCourseCalculation> findByUserIdAndStandardGraduationRequirements(Integer id, StandardGraduationRequirements standardGraduationRequirements);
}
