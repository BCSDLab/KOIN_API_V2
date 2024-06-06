package in.koreatech.koin.domain.timetable.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.exception.TimeTableNotFoundException;
import in.koreatech.koin.domain.timetable.model.TimeTable;

public interface TimeTableRepository extends Repository<TimeTable, Integer> {

    TimeTable save(TimeTable timeTable);

    List<TimeTable> findAllByUserId(Integer id);

    List<TimeTable> findAllByUserIdAndSemesterId(Integer userId, Integer semesterId);

    Optional<TimeTable> findById(Integer id);

    void deleteByUserIdAndSemesterId(Integer userId, Integer semesterId);

    default TimeTable getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> TimeTableNotFoundException.withDetail("id: " + id));
    }
}
