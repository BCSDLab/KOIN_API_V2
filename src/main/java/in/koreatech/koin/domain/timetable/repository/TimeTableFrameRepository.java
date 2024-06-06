package in.koreatech.koin.domain.timetable.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.exception.TimeTableNotFoundException;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetable.model.TimeTableFrame;

public interface TimeTableFrameRepository extends Repository<TimeTableFrame, Integer> {

    Optional<TimeTableFrame> findById(Integer id);

    default TimeTableFrame getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> TimeTableNotFoundException.withDetail("id: " + id));
    }

    List<TimeTableFrame> findAllByUserIdAndSemester(Integer userId, String semester);
}
