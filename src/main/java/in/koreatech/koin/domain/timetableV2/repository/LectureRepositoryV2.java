package in.koreatech.koin.domain.timetableV2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.exception.LectureNotFoundException;
import in.koreatech.koin.domain.timetable.exception.SemesterNotFoundException;
import in.koreatech.koin.domain.timetable.model.Lecture;

public interface LectureRepositoryV2 extends Repository<Lecture, Integer> {

    Lecture save(Lecture lecture);

    Optional<Lecture> findById(Integer id);

    Optional<Lecture> findBySemesterAndNameAndLectureClass(String semesterDate, String name, String classLecture);

    default Lecture getBySemesterAndNameAndLectureClass(String semesterDate, String name, String classLecture) {
        return findBySemesterAndNameAndLectureClass(semesterDate, name, classLecture)
            .orElseThrow(() -> SemesterNotFoundException.withDetail("semester: " + semesterDate + " name: " + name + " classLecture: " + classLecture));
    }

    default Lecture getLectureById(Integer id) {
        return findById(id)
            .orElseThrow(() -> LectureNotFoundException.withDetail("lecture_id: " + id));
    }

    List<Lecture> findByIdIn(List<Integer> lectureIds);
}
