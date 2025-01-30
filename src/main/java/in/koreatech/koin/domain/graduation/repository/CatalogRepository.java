package in.koreatech.koin.domain.graduation.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.graduation.exception.CatalogNotFoundException;
import in.koreatech.koin.domain.graduation.model.Catalog;
import in.koreatech.koin.domain.student.model.Department;

public interface CatalogRepository extends Repository<Catalog, Integer> {

    Optional<Catalog> findByDepartmentAndCode(Department department, String code);

    List<Catalog> findByLectureNameAndMajorIdAndYear(String lectureName, Integer majorId, String year);

    List<Catalog> findByLectureNameAndDepartmentIdAndYear(String lectureName, Integer departmentId, String year);

    Optional<List<Catalog>> findAllByCourseTypeId(Integer courseTypeId);

    default Catalog getByDepartmentAndCode(Department department, String code) {
        return findByDepartmentAndCode(department, code)
            .orElseThrow(() -> CatalogNotFoundException.withDetail("department: " + department + ", code: " + code));
    }

    default List<Catalog> getAllByCourseTypeId(Integer courseTypeId) {
        return findAllByCourseTypeId(courseTypeId)
            .orElseThrow(() -> CatalogNotFoundException.withDetail("course_type_id" + courseTypeId));
    }
    List<Catalog> findByLectureNameAndDepartment(String lectureName, Department department);
}
