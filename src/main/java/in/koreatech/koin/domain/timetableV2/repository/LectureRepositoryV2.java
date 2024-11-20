package in.koreatech.koin.domain.timetableV2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.exception.LectureNotFoundException;
import in.koreatech.koin.domain.timetable.exception.SemesterNotFoundException;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;

public interface LectureRepositoryV2 extends Repository<Lecture, Integer> {

    List<Lecture> findBySemester(Semester semester);

    Lecture save(Lecture lecture);

    Optional<Lecture> findById(Integer id);

    Optional<Lecture> findBySemesterAndCodeAndLectureClass(Semester semester, String code, String classLecture);

    default Lecture getBySemesterAndCodeAndLectureClass(Semester semester, String code, String classLecture) {
        return findBySemesterAndCodeAndLectureClass(semester, code, classLecture)
            .orElseThrow(() -> SemesterNotFoundException.withDetail(
                "semester: " + semester.getSemester() + " code: " + code + " classLecture: " + classLecture));
    }

    default Lecture getLectureById(Integer id) {
        return findById(id)
            .orElseThrow(() -> LectureNotFoundException.withDetail("lecture_id: " + id));
    }
}
