package in.koreatech.koin.domain.timetable.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.exception.LectureNotFoundException;
import in.koreatech.koin.domain.timetable.exception.SemesterNotFoundException;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.global.config.repository.JpaRepository;

@JpaRepository
public interface LectureRepository extends Repository<Lecture, Integer> {

    List<Lecture> findBySemester(String semesterDate);

    Lecture save(Lecture lecture);

    Optional<Lecture> findById(Integer id);

    Optional<Lecture> findBySemesterAndCodeAndLectureClass(String semesterDate, String code, String classLecture);

    default Lecture getBySemesterAndCodeAndLectureClass(String semesterDate, String code, String classLecture) {
        return findBySemesterAndCodeAndLectureClass(semesterDate, code, classLecture)
            .orElseThrow(() -> SemesterNotFoundException.withDetail("semester: " + semesterDate + " code: " + code + " classLecture: " + classLecture));
    }

    default Lecture getLectureById(Integer id) {
        return findById(id)
            .orElseThrow(() -> LectureNotFoundException.withDetail("lecture_id: " + id));
    }

    List<Lecture> findByIdIn(List<Integer> lectureIds);
}
