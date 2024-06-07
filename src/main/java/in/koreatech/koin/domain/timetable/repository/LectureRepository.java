package in.koreatech.koin.domain.timetable.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.model.Lecture;
import io.lettuce.core.dynamic.annotation.Param;

public interface LectureRepository extends Repository<Lecture, Integer> {

    List<Lecture> findBySemester(String semesterDate);

    Lecture save(Lecture lecture);

    @Query("SELECT l FROM Lecture l WHERE l.id IN :lectureIds")
    List<Lecture> findAllByIds(@Param("lectureIds") List<Integer> lectureIds);
}
