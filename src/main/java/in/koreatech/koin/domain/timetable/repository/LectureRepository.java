package in.koreatech.koin.domain.timetable.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import in.koreatech.koin.domain.timetable.model.Lecture;

public interface LectureRepository extends JpaRepository<Lecture, Long> {

    List<Lecture> findBySemester(String semesterDate);
}
