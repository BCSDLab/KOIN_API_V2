package in.koreatech.koin.domain.collegecredit.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.collegecredit.model.CourseType;
import in.koreatech.koin.domain.collegecredit.model.StandardGraduationRequirements;

public interface StandardGraduationRequirementsRepository extends Repository<StandardGraduationRequirements, Integer> {
    Optional<StandardGraduationRequirements> findByYearAndDepartment(String year, String department);
}
