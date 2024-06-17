package in.koreatech.koin.domain.timetable.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.exception.TimeTableNotFoundException;
import in.koreatech.koin.domain.timetable.model.TimetableFrame;

public interface TimetableFrameRepository extends Repository<TimetableFrame, Integer> {

    List<TimetableFrame> findAllByUserId(Integer userId);

    List<TimetableFrame> findAllByUserIdAndIsMain(Integer userId, Boolean isMain);

    Optional<TimetableFrame> findById(Integer id);

    Optional<TimetableFrame> findByUserIdAndSemesterIdAndIsMain(Integer userId, Integer semesterId, boolean isMain);

    default TimetableFrame getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> TimeTableNotFoundException.withDetail("timetable_frame_id : " + id));
    }

    default TimetableFrame getByUserIdAndSemesterAndIsMain(Integer userId, Integer semesterId, boolean isMain) {
        return findByUserIdAndSemesterIdAndIsMain(userId, semesterId, isMain)
            .orElseThrow(() -> TimeTableNotFoundException.withDetail("userId: " + userId));
    }
}
