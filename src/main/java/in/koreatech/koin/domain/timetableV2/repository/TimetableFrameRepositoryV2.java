package in.koreatech.koin.domain.timetableV2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.timetable.exception.TimetableNotFoundException;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV2.exception.TimetableFrameNotFoundException;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.config.repository.JpaRepositoryMarker;
import jakarta.persistence.LockModeType;

@JpaRepositoryMarker
public interface TimetableFrameRepositoryV2 extends Repository<TimetableFrame, Integer> {

    Optional<TimetableFrame> findById(Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT t FROM TimetableFrame t
        WHERE t.id = :id
        """)
    Optional<TimetableFrame> findByIdWithLock(@Param("id") Integer id);

    List<TimetableFrame> findByUserIdAndIsMainTrue(Integer userId);

    Optional<TimetableFrame> findByUserIdAndSemesterIdAndIsMainTrue(Integer userId, Integer semesterId);

    default TimetableFrame getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> TimetableNotFoundException.withDetail("id: " + id));
    }

    default TimetableFrame getByIdWithLock(Integer id) {
        return findByIdWithLock(id)
            .orElseThrow(() -> TimetableNotFoundException.withDetail("id: " + id));
    }

    default TimetableFrame getMainTimetableFrame(Integer userId, Integer semesterId) {
        return findByUserIdAndSemesterIdAndIsMainTrue(userId, semesterId)
            .orElseThrow(
                () -> TimetableFrameNotFoundException.withDetail("userId: " + userId + ", semesterId: " + semesterId));
    }

    List<TimetableFrame> findAllByUserIdAndSemesterId(Integer userId, Integer semesterId);

    TimetableFrame save(TimetableFrame timetableFrame);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
            SELECT t FROM TimetableFrame t
            WHERE t.user.id = :userId
            AND t.semester.id = :semesterId
            AND t.isMain = false
            ORDER BY t.createdAt ASC
            LIMIT 1
            """)
    TimetableFrame findNextFirstTimetableFrame(@Param("userId") Integer userId,
        @Param("semesterId") Integer semesterId);

    @Query(
        """
            SELECT COUNT(t) FROM TimetableFrame t
            WHERE t.user.id = :userId
            AND t.semester.id = :semesterId
            """)
    int countByUserIdAndSemesterId(@Param("userId") Integer userId, @Param("semesterId") Integer semesterId);

    void deleteById(Integer id);

    List<TimetableFrame> findAllByUserAndSemester(User user, Semester semester);

    void deleteAllByUser(User user);

    @Query(value = "SELECT * FROM timetable_frame WHERE id = :id", nativeQuery = true)
    Optional<TimetableFrame> findByIdWithDeleted(@Param("id") Integer id);

    default TimetableFrame getByIdWithDeleted(Integer id) {
        return findByIdWithDeleted(id)
            .orElseThrow(() -> TimetableFrameNotFoundException.withDetail("id: " + id));
    }

    Optional<List<TimetableFrame>> findAllByUserId(Integer userId);

    boolean existsByUserAndSemester(User user, Semester semester);

    default List<TimetableFrame> getAllByUserId(Integer userId) {
        return findAllByUserId(userId)
            .orElseThrow(() -> TimetableFrameNotFoundException.withDetail("userId: " + userId));
    }
}
