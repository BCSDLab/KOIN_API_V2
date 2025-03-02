package in.koreatech.koin.domain.graduation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.graduation.model.StandardGraduationRequirements;
import in.koreatech.koin.domain.student.model.Major;

public interface StandardGraduationRequirementsRepository extends Repository<StandardGraduationRequirements, Integer> {

    List<StandardGraduationRequirements> findAllByMajorAndYear(Major major, String year);

    List<StandardGraduationRequirements> findByMajorIdAndCourseTypeIdAndYear(Integer id, Integer id1, String studentYear);

    List<StandardGraduationRequirements> findByCourseTypeIdAndYear(Integer id, String studentYear);

    Optional<StandardGraduationRequirements> findFirstByMajorIdAndCourseTypeIdAndYear(Integer id, Integer id1, String studentYear);
}
