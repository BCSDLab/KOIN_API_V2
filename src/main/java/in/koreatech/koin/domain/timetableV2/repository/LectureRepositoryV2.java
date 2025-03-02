package in.koreatech.koin.domain.timetableV2.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.exception.LectureNotFoundException;
import in.koreatech.koin.domain.timetable.model.Lecture;
import io.lettuce.core.dynamic.annotation.Param;

public interface LectureRepositoryV2 extends Repository<Lecture, Integer> {

    List<Lecture> findBySemester(String semesterDate);

    Lecture save(Lecture lecture);

    Optional<Lecture> findById(Integer id);

    List<Lecture> findAllBySemesterInAndCodeIn(Set<String> semesters, Set<String> codes);

    @Query("SELECT l FROM Lecture l WHERE l.code IN :codes AND l.semester = :semesterDate")
    Optional<List<Lecture>> findAllByCodesAndSemester(@Param("codes") List<String> codes, @Param("semesterDate") String semesterDate);

    default Lecture getLectureById(Integer id) {
        return findById(id)
            .orElseThrow(() -> LectureNotFoundException.withDetail("lecture_id: " + id));
    }
}
