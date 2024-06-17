package in.koreatech.koin.domain.timetable.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.model.Lecture;

public interface LectureRepository extends Repository<Lecture, Integer> {

    List<Lecture> findBySemester(String semesterDate);

    Lecture save(Lecture lecture);

    List<Lecture> findByIdIn(List<Integer> lectureIds);
}
