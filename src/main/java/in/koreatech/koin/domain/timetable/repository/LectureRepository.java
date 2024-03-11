package in.koreatech.koin.domain.timetable.repository;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

import in.koreatech.koin.domain.timetable.model.Lecture;

public interface LectureRepository extends JpaRepository<Lecture, Long> {
    ArrayList<Lecture> findBySemester(String semesterDate);
    Boolean existsBySemester(String semesterDate);
}
