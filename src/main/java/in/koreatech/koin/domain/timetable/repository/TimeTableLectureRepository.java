package in.koreatech.koin.domain.timetable.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.model.TimeTableFrame;
import in.koreatech.koin.domain.timetable.model.TimeTableLecture;

public interface TimeTableLectureRepository extends Repository<TimeTableLecture, Integer> {

    TimeTableLecture save(TimeTableLecture lecture);

    List<TimeTableLecture> findAllByTimetableFrame(TimeTableFrame frame);
}
