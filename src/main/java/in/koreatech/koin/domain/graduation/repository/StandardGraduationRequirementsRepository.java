package in.koreatech.koin.domain.graduation.repository;

import in.koreatech.koin.domain.graduation.model.Department;
import in.koreatech.koin.domain.graduation.model.StandardGraduationRequirements;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface StandardGraduationRequirementsRepository extends Repository<StandardGraduationRequirements, Integer> {

    List<StandardGraduationRequirements> findAllByDepartmentAndYear(Department department, String year);
}
