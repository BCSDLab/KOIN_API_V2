package in.koreatech.koin.domain.timetable.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.timetable.exception.TimetablesFrameNotFoundException;
import in.koreatech.koin.domain.timetable.model.TimeTableFrame;
import in.koreatech.koin.domain.user.model.User;

public interface TimeTableFrameRepository extends Repository<TimeTableFrame, Integer> {

    TimeTableFrame save(TimeTableFrame timetablesFrame);

    Optional<TimeTableFrame> findByUser(User user);

    default TimeTableFrame getByUser(User user) {
        return findByUser(user)
            .orElseThrow(() -> TimetablesFrameNotFoundException.withDetail("userId: " + user.getId()));
    }

    Optional<TimeTableFrame> findById(Integer frameId);

    default TimeTableFrame getById(Integer frameId) {
        return findById(frameId)
            .orElseThrow(() -> TimetablesFrameNotFoundException.withDetail("timetablesFrameId: " + frameId));
    }

    List<TimeTableFrame> findAllByUserIdAndSemesterId(Integer userId, Integer semesterId);

    @Query(
        """
        SELECT COUNT(t) FROM TimeTableFrame t
        WHERE t.user.id = :userId
        AND t.semester.id = :semesterId
        """)
    int countByUserIdAndSemesterId(@Param("userId") Integer userId, @Param("semesterId") Integer semesterId);


    void deleteById(Integer id);

    void deleteAllByUser(User user);
}
