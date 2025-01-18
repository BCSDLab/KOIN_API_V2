package in.koreatech.koin.domain.graduation.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.graduation.model.StandardGraduationRequirements;
import in.koreatech.koin.domain.student.model.Major;

public interface StandardGraduationRequirementsRepository extends Repository<StandardGraduationRequirements, Integer> {

    List<StandardGraduationRequirements> findAllByMajorAndYear(Major major, String year);

    List<StandardGraduationRequirements> findByDepartmentIdAndCourseTypeIdAndYear(Integer id, Integer id1, String studentYear);
}
