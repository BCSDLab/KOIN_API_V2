package in.koreatech.koin.domain.graduation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.graduation.exception.CatalogNotFoundException;
import in.koreatech.koin.domain.graduation.model.Catalog;
import in.koreatech.koin.domain.student.model.Department;

public interface CatalogRepository extends Repository<Catalog, Integer> {

    List<Catalog> findAllByCode(String code);

    Optional<Catalog> findByDepartmentAndCode(Department department, String code);

    Optional<Catalog> findByCodeAndYear(String code, String year);

    Optional<Catalog> findFirstByCodeAndYearOrderByCreatedAtDesc(String code, String year);

    Optional<Catalog> findFirstByLectureNameAndYearOrderByCreatedAtDesc(String lectureName, String year);

    // 이거 오류나요..
    // List<Catalog> findByLectureNameAndYearAndDepartment(String lectureName, String studentYear, Department department);

    Optional<List<Catalog>> findAllByCourseTypeId(Integer courseTypeId);

    default Catalog getByDepartmentAndCode(Department department, String code) {
        return findByDepartmentAndCode(department, code)
            .orElseThrow(() -> CatalogNotFoundException.withDetail("department: " + department + ", code: " + code));
    }

    default List<Catalog> getAllByCourseTypeId(Integer courseTypeId) {
        return findAllByCourseTypeId(courseTypeId)
            .orElseThrow(() -> CatalogNotFoundException.withDetail("course_type_id" + courseTypeId));
    }

    default Catalog getByAndCodeAndYear(String code, String year) {
        return findByCodeAndYear(code, year)
            .orElseThrow(() -> CatalogNotFoundException.withDetail("code: " + code + ", year: " + year));
    }

    List<Catalog> findByLectureNameAndDepartment(String lectureName, Department department);
}
