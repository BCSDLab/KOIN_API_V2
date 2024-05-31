package in.koreatech.koin.domain.timetable.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.model.TimeTableLecture;

public interface TimeTableLectureRepository extends Repository<TimeTableLecture, Integer> {
}
