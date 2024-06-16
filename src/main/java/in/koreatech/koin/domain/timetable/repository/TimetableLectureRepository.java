package in.koreatech.koin.domain.timetable.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.model.TimetableFrame;
import in.koreatech.koin.domain.timetable.model.TimetableLecture;

public interface TimetableLectureRepository extends Repository<TimetableLecture, Integer> {

    TimetableLecture save(TimetableLecture lecture);

    List<TimetableLecture> findAllByTimetableFrame(TimetableFrame frame);

    Optional<TimetableLecture> findById(Integer id);
}
