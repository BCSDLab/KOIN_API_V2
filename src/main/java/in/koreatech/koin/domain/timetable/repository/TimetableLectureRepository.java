package in.koreatech.koin.domain.timetable.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.exception.TimetableNotFoundException;
import in.koreatech.koin.domain.timetable.model.TimetableLecture;

public interface TimetableLectureRepository extends Repository<TimetableLecture, Integer> {
    Optional<TimetableLecture> findById(Integer id);

    List<TimetableLecture> findAllByTimetableFrameId(Integer id);

    TimetableLecture save(TimetableLecture timeTableLecture);

    default TimetableLecture getById(Integer id) {
        return findById(id).orElseThrow(() -> TimetableNotFoundException.withDetail("TimeTableLectureId : " + id));
    }
}
