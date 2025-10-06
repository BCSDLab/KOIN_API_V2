package in.koreatech.koin.domain.timetableV2.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.exception.LectureNotFoundException;
import in.koreatech.koin.domain.timetable.exception.SemesterNotFoundException;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.config.repository.JpaRepository;
import io.lettuce.core.dynamic.annotation.Param;

@JpaRepository
public interface LectureRepositoryV2 extends Repository<Lecture, Integer> {

    List<Lecture> findBySemester(String semesterDate);

    Lecture save(Lecture lecture);

    Optional<Lecture> findById(Integer id);

    Optional<Lecture> findBySemesterAndCodeAndLectureClass(String semesterDate, String code, String classLecture);

    List<Lecture> findAllBySemesterInAndCodeIn(Set<String> semesters, Set<String> codes);

    @Query("SELECT l FROM Lecture l WHERE l.code IN :codes AND l.semester = :semesterDate")
    Optional<List<Lecture>> findAllByCodesAndSemester(@Param("codes") List<String> codes, @Param("semesterDate") String semesterDate);

    default Lecture getBySemesterAndCodeAndLectureClass(String semesterDate, String code, String classLecture) {
        return findBySemesterAndCodeAndLectureClass(semesterDate, code, classLecture)
            .orElseThrow(() -> SemesterNotFoundException.withDetail(
                "semester: " + semesterDate + " code: " + code + " classLecture: " + classLecture));
    }

    default Lecture getLectureById(Integer id) {
        return findById(id)
            .orElseThrow(() -> LectureNotFoundException.withDetail("lecture_id: " + id));
    }
}
