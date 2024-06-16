package in.koreatech.koin.domain.timetable.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.model.TimetableLecture;

public interface TimetableLectureRepository extends Repository<TimetableLecture, Integer> {

    TimetableLecture save(TimetableLecture timetableLecture);

    List<TimetableLecture> findAllByTimetableFrameId(Integer timetableFrameId);
}
