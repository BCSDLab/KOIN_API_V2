package in.koreatech.koin.domain.graduation.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.graduation.exception.CatalogNotFoundException;
import in.koreatech.koin.domain.graduation.model.Catalog;
import in.koreatech.koin.domain.student.model.Department;

public interface CatalogRepository extends Repository<Catalog, Integer> {
    Optional<Catalog> findByYearAndDepartmentAndCode(String year, Department department, String code);

    default Catalog getByYearAndDepartmentAndCode(String year, Department department, String code) {
        return findByYearAndDepartmentAndCode(year, department, code)
            .orElseThrow(() -> CatalogNotFoundException.withDetail(
                "year: " + year + ", department: " + department + ", code: " + code));
    }
}
