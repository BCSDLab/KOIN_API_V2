package in.koreatech.koin.domain.timetable.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.exception.TimeTableNotFoundException;
import in.koreatech.koin.domain.timetable.exception.TimetableLectureNotFoundException;
import in.koreatech.koin.domain.timetable.model.TimeTable;
import in.koreatech.koin.domain.timetable.model.TimeTableLecture;

public interface TimeTableLectureRepository extends Repository<TimeTableLecture, Integer> {

    Optional<TimeTableLecture> findById(Integer id);

    void deleteById(Integer id);

    default TimeTableLecture getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> TimetableLectureNotFoundException.withDetail("id: " + id));
    }
}
