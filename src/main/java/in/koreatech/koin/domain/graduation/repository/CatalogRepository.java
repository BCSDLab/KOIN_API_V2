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
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.config.repository.JpaRepository;

@JpaRepository
public interface CatalogRepository extends Repository<Catalog, Integer> {

    @EntityGraph(attributePaths = {"courseType"}, type = LOAD)
    @Query("SELECT c FROM Catalog c WHERE c.lectureName IN :lectureNames AND c.year = :year")
    List<Catalog> findAllByLectureNameInAndYear(Set<String> lectureNames, String year);

    @EntityGraph(attributePaths = {"courseType"}, type = LOAD)
    @Query("SELECT c FROM Catalog c WHERE c.code IN :lectureCodes AND c.year IN :year")
    List<Catalog> findAllByCodeInAndYearIn(Set<String> lectureCodes, Set<String> year);

    Optional<Catalog> findByDepartmentAndCode(Department department, String code);

    Optional<List<Catalog>> findAllByCourseTypeId(Integer courseTypeId);

    List<Catalog> findByYearAndCode(String year, String code);

    List<Catalog> findByLectureNameAndYear(String lectureName, String year);

    default List<Catalog> getAllByCourseTypeId(Integer courseTypeId) {
        return findAllByCourseTypeId(courseTypeId)
            .orElseThrow(() -> CatalogNotFoundException.withDetail("course_type_id" + courseTypeId));
    }

    List<Catalog> findByLectureNameAndYearAndMajor(String lectureName, String studentYear, Major major);

    List<Catalog> findByLectureNameOrderByYearDesc(String lectureName);

    List<Catalog> findByLectureNameAndYearIn(String lectureName, List<String> attendedYears);
}
