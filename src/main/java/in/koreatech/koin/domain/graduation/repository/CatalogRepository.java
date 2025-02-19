package in.koreatech.koin.domain.graduation.repository;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.LOAD;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.graduation.exception.CatalogNotFoundException;
import in.koreatech.koin.domain.graduation.model.Catalog;
import in.koreatech.koin.domain.student.model.Department;

public interface CatalogRepository extends Repository<Catalog, Integer> {

    @EntityGraph(attributePaths = {"courseType"}, type = LOAD)
    @Query("SELECT c FROM Catalog c WHERE c.lectureName IN :lectureNames AND c.year = :year")
    List<Catalog> findAllByLectureNameInAndYear(Set<String> lectureNames, String year);

    @EntityGraph(attributePaths = {"courseType"}, type = LOAD)
    @Query("SELECT c FROM Catalog c WHERE c.code IN :lectureCodes AND c.year IN :year")
    List<Catalog> findAllByCodeInAndYearIn(Set<String> lectureCodes, Set<String> year);

    Optional<Catalog> findByDepartmentAndCode(Department department, String code);

    List<Catalog> findByLectureNameAndMajorIdAndYear(String lectureName, Integer majorId, String year);

    List<Catalog> findByLectureNameAndDepartmentIdAndYear(String lectureName, Integer departmentId, String year);

    Optional<Catalog> findByCodeAndYear(String code, String year);

    Optional<List<Catalog>> findAllByCourseTypeId(Integer courseTypeId);

    List<Catalog> findByYearAndCode(String year, String code);

    List<Catalog> findByLectureNameAndYear(String lectureName, String year);

    Optional<Catalog> findByYearAndCodeAndLectureName(String year, String code, String lectureName);

    default Catalog getByDepartmentAndCode(Department department, String code) {
        return findByDepartmentAndCode(department, code)
            .orElseThrow(() -> CatalogNotFoundException.withDetail("department: " + department + ", code: " + code));
    }

    default List<Catalog> getAllByCourseTypeId(Integer courseTypeId) {
        return findAllByCourseTypeId(courseTypeId)
            .orElseThrow(() -> CatalogNotFoundException.withDetail("course_type_id" + courseTypeId));
    }

    List<Catalog> findByLectureNameAndDepartment(String lectureName, Department department);

    List<Catalog> findAllByYearAndCourseTypeId(String year, Integer courseTypeId);
}
