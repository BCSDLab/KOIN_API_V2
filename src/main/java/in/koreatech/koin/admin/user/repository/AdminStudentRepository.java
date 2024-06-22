package in.koreatech.koin.admin.user.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.admin.user.dto.StudentsCondition;
import in.koreatech.koin.domain.user.exception.UserNotFoundException;
import in.koreatech.koin.domain.user.model.Student;

public interface AdminStudentRepository extends Repository<Student, Integer> {

    Student save(Student student);

    Optional<Student> findById(Integer userId);

    default Student getById(Integer userId) {
        return findById(userId)
            .orElseThrow(() -> UserNotFoundException.withDetail("userId: " + userId));
    }

    @Query(" SELECT COUNT(s) FROM Student s ")
    Integer findAllStudentCount();

    @Query("SELECT s FROM Student s WHERE " +
        "(:#{#condition.isAuthed} IS NULL OR s.user.isAuthed = :#{#condition.isAuthed}) AND " +
        "(:#{#condition.nickname} IS NULL OR s.user.nickname LIKE CONCAT('%', :#{#condition.nickname}, '%')) AND " +
        "(:#{#condition.email} IS NULL OR s.user.email LIKE CONCAT('%', :#{#condition.email}, '%'))")
    Page<Student> findByConditions(@Param("condition") StudentsCondition condition, Pageable pageable);
}
