package in.koreatech.koin.domain.timetable.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.exception.TimetableFrameNotFoundException;
import in.koreatech.koin.domain.timetable.exception.TimetableNotFoundException;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetable.model.TimetableFrame;

public interface TimetableFrameRepository extends Repository<TimetableFrame, Integer> {

    Optional<TimetableFrame> findById(Integer id);

    List<TimetableFrame> findAllByUserId(Integer userid);

    Optional<TimetableFrame> findByUserIdAndSemesterIdAndIsMain(Integer id, Integer semesterId, boolean isMain);

    default TimetableFrame getById(Integer id) {
        return findById(id).orElseThrow(() -> TimetableNotFoundException.withDetail("TimeTableFrameId :" + id));
    }

    default TimetableFrame getByUserIdAndSemesterId(Integer userId, Integer semesterId, boolean isMain) {
        return findByUserIdAndSemesterIdAndIsMain(userId, semesterId, isMain)
            .orElseThrow(() -> TimetableFrameNotFoundException.withDetail("userId: " + userId));
    }
}
