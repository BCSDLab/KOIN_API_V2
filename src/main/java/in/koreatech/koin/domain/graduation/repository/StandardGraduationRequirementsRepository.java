package in.koreatech.koin.domain.graduation.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.graduation.model.StandardGraduationRequirements;
import in.koreatech.koin.domain.student.model.Department;

public interface StandardGraduationRequirementsRepository extends Repository<StandardGraduationRequirements, Integer> {

    List<StandardGraduationRequirements> findAllByDepartmentAndYear(Department department, String year);
}
