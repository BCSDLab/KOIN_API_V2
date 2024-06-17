package in.koreatech.koin.domain.timetable.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.timetable.exception.TimetableFrameNotFoundException;
import in.koreatech.koin.domain.timetable.model.TimetableFrame;
import in.koreatech.koin.domain.user.model.User;

public interface TimetableFrameRepository extends Repository<TimetableFrame, Integer> {

    TimetableFrame save(TimetableFrame timetableFrame);

    Optional<TimetableFrame> findByUser(User user);

    default TimetableFrame getByUser(User user) {
        return findByUser(user)
            .orElseThrow(() -> TimetableFrameNotFoundException.withDetail("userId: " + user.getId()));
    }

    Optional<TimetableFrame> findById(Integer frameId);

    default TimetableFrame getById(Integer frameId) {
        return findById(frameId)
            .orElseThrow(() -> TimetableFrameNotFoundException.withDetail("timetablesFrameId: " + frameId));
    }

    List<TimetableFrame> findAllByUserIdAndSemesterId(Integer userId, Integer semesterId);

    TimetableFrame findFirstByUserIdAndSemesterIdAndIsMainFalseOrderByCreatedAtAsc(Integer userId, Integer semesterId);

    @Query(
        """
        SELECT COUNT(t) FROM TimetableFrame t
        WHERE t.user.id = :userId
        AND t.semester.id = :semesterId
        """)
    int countByUserIdAndSemesterId(@Param("userId") Integer userId, @Param("semesterId") Integer semesterId);

    void deleteById(Integer id);

    void deleteAllByUser(User user);
}
