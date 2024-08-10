package in.koreatech.koin.domain.collegecredit.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.collegecredit.model.StandardGraduationRequirements;

public interface StandardGraduationRequirementsRepository extends Repository<StandardGraduationRequirements, Integer> {
    List<StandardGraduationRequirements> findByYearAndDepartment(String year, String department);
}
