package in.koreatech.koin.domain.timetable.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.exception.TimeTableNotFoundException;
import in.koreatech.koin.domain.timetable.model.TimeTable;

public interface TimeTableRepository extends Repository<TimeTable, Long> {

    TimeTable save(TimeTable timeTable);

    List<TimeTable> findByUserIdAndSemesterId(Long userId, Long semesterId);

    Optional<TimeTable> findById(Long id);

    void deleteByUserIdAndSemesterId(Long userId, Long semesterId);

    default List<TimeTable> getByUserIdAndSemesterId(Long userId, Long semesterId) {
        return findByUserIdAndSemesterId(userId, semesterId);
    }

    default TimeTable getById(Long id) {
        return findById(id)
            .orElseThrow(() -> TimeTableNotFoundException.withDetail("id: " + id));
    }
}
