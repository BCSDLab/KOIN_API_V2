package in.koreatech.koin.domain.timetableV3.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.exception.LectureNotFoundException;
import in.koreatech.koin.domain.timetable.model.Lecture;
import io.lettuce.core.dynamic.annotation.Param;

public interface LectureRepositoryV3 extends Repository<Lecture, Integer> {

    List<Lecture> findBySemester(String semesterDate);

    Optional<Lecture> findById(Integer id);

    default Lecture getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> LectureNotFoundException.withDetail("lecture_id: " + id));
    }
}
