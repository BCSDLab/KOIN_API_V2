package in.koreatech.koin.domain.graduation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.graduation.model.StandardGraduationRequirements;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.config.repository.JpaRepository;

@JpaRepository
public interface StandardGraduationRequirementsRepository extends Repository<StandardGraduationRequirements, Integer> {

    List<StandardGraduationRequirements> findAllByMajorAndYear(Major major, String year);

    Optional<StandardGraduationRequirements> findFirstByMajorIdAndCourseTypeIdAndYear(Integer id, Integer id1, String studentYear);

    boolean existsByMajorIdAndYear(Integer id, String studentYear);
}
