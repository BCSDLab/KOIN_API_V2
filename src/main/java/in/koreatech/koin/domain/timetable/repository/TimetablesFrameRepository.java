package in.koreatech.koin.domain.timetable.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.timetable.exception.TimetablesFrameNotFoundException;
import in.koreatech.koin.domain.timetable.model.TimeTable;
import in.koreatech.koin.domain.timetable.model.TimetablesFrame;

public interface TimetablesFrameRepository extends Repository<TimetablesFrame, Integer> {

    @Query(
        """
        SELECT COUNT(t) FROM TimetablesFrame t
        WHERE t.user.id = :userId
        AND t.semester.id = :semesterId
        """)
    int countByUserIdAndSemesterId(@Param("userId") Integer userId, @Param("semesterId") String semesterId);

    TimetablesFrame save(TimetablesFrame timetablesFrame);

    List<TimetablesFrame> findAllByUserIdAndSemesterId(Integer userId, Integer semesterId);

    void deleteById(Integer id);

    Optional<TimetablesFrame> findById(Integer frameId);

    default TimetablesFrame getById(Integer frameId) {
        return findById(frameId)
            .orElseThrow(() -> TimetablesFrameNotFoundException.withDetail("timetablesFrameId: " + frameId));
    }
}
