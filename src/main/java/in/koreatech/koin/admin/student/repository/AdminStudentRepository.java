package in.koreatech.koin.admin.student.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.global.config.repository.JpaRepository;
import in.koreatech.koin.global.exception.CustomException;
import in.koreatech.koin.global.code.ApiResponseCode;
import in.koreatech.koin.admin.student.dto.StudentsCondition;
import in.koreatech.koin.domain.student.model.Student;

@JpaRepository
public interface AdminStudentRepository extends Repository<Student, Integer> {

    Student save(Student student);

    void deleteById(Integer userId);

    Optional<Student> findById(Integer userId);

    default Student getById(Integer userId) {
        return findById(userId)
            .orElseThrow(() -> CustomException.of(ApiResponseCode.NOT_FOUND_USER, "userId: " + userId));
    }

    @Query(" SELECT COUNT(s) FROM Student s ")
    Integer findAllStudentCount();

    @Query(
        """
            SELECT s FROM Student s WHERE\s
            (:#{#condition.isAuthed} IS NULL OR s.user.isAuthed = :#{#condition.isAuthed}) AND
            (:#{#condition.nickname} IS NULL OR s.user.nickname LIKE CONCAT('%', :#{#condition.nickname}, '%')) AND
            (:#{#condition.email} IS NULL OR s.user.email LIKE CONCAT('%', :#{#condition.email}, '%'))
            """)
    Page<Student> findByConditions(@Param("condition") StudentsCondition condition, Pageable pageable);
}
