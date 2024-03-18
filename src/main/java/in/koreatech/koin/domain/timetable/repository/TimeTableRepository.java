package in.koreatech.koin.domain.timetable.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.exception.TimeTableNotFoundException;
import in.koreatech.koin.domain.timetable.model.TimeTable;

public interface TimeTableRepository extends Repository<TimeTable, Long> {

    TimeTable save(TimeTable timeTable);

    List<TimeTable> findByUserIdAndSemesterId(Long userID, Long semesterId);

    Optional<TimeTable> findById(Long id);

    default List<TimeTable> getByUserIdAndSemesterId(Long userId, Long semesterId){

        List<TimeTable> timeTables = findByUserIdAndSemesterId(userId, semesterId);

        if(timeTables.isEmpty()){
            throw new TimeTableNotFoundException("userId, semesterId: " + userId + ", " + semesterId);
        }

        return timeTables;
    }
}
