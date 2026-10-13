package in.koreatech.koin.domain.student.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.student.model.AcademicProgramYear;

public interface AcademicProgramYearRepository extends Repository<AcademicProgramYear, Integer> {

    @EntityGraph(attributePaths = {"department", "major"})
    List<AcademicProgramYear> findAllByStudentYearOrderByDepartmentIdAscIdAsc(String studentYear);
}
