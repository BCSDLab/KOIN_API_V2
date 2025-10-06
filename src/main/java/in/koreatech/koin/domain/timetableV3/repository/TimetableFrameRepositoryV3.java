package in.koreatech.koin.domain.timetableV3.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.timetable.exception.TimetableNotFoundException;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV2.exception.TimetableFrameNotFoundException;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.config.repository.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface TimetableFrameRepositoryV3 extends Repository<TimetableFrame, Integer> {

    Optional<TimetableFrame> findById(Integer id);

    default TimetableFrame getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> TimetableNotFoundException.withDetail("id: " + id));
    }

    List<TimetableFrame> findByUserIdAndIsMainTrue(Integer userId);

    List<TimetableFrame> findAllByUserIdAndIsMainTrue(Integer userId);

    @Query(value = "SELECT * FROM timetable_frame WHERE id = :id", nativeQuery = true)
    Optional<TimetableFrame> findByIdWithDeleted(@Param("id") Integer id);

    default TimetableFrame getByIdWithDeleted(Integer id) {
        return findByIdWithDeleted(id)
            .orElseThrow(() -> TimetableFrameNotFoundException.withDetail("id: " + id));
    }

    boolean existsByUserAndSemester(User user, Semester semester);

    @Query(
        """
            SELECT COUNT(t) FROM TimetableFrame t
            WHERE t.user.id = :userId
            AND t.semester.id = :semesterId
            """)
    int countByUserIdAndSemesterId(@Param("userId") Integer userId, @Param("semesterId") Integer semesterId);

    void save(TimetableFrame timetableFrame);

    List<TimetableFrame> findByUserAndSemester(User user, Semester semester);

    Optional<TimetableFrame> findByUserIdAndSemesterIdAndIsMainTrue(Integer userId, Integer semesterId);

    default TimetableFrame getMainTimetableByUserIdAndSemesterId(Integer userId, Integer semesterId) {
        return findByUserIdAndSemesterIdAndIsMainTrue(userId, semesterId)
            .orElseThrow(
                () -> TimetableFrameNotFoundException.withDetail("userId: " + userId + ", semesterId: " + semesterId));
    }

    List<TimetableFrame> findAllByUserIdAndSemesterId(Integer userId, Integer semesterId);

    List<TimetableFrame> findAllByUserId(Integer userId);

    List<TimetableFrame> findAllByUserAndSemester(User user, Semester semester);
}
