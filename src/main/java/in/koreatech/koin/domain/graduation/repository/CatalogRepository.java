package in.koreatech.koin.domain.graduation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.graduation.exception.CatalogNotFoundException;
import in.koreatech.koin.domain.graduation.model.Catalog;
import in.koreatech.koin.domain.student.model.Department;

public interface CatalogRepository extends Repository<Catalog, Integer> {

    Optional<Catalog> findByDepartmentAndCode(Department department, String code);

    default Catalog getByYearAndDepartmentAndCode(Department department, String code) {
        return findByDepartmentAndCode(department, code)
            .orElseThrow(() -> CatalogNotFoundException.withDetail("department: " + department + ", code: " + code));
    }

    List<Catalog> findByLectureNameAndYearAndDepartment(String lectureName, String studentYear, Department department);
}
