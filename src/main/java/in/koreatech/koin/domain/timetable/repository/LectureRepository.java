package in.koreatech.koin.domain.timetable.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.model.Lecture;

public interface LectureRepository extends Repository<Lecture, Integer> {

    Lecture save(Lecture lecture);

    Optional<Lecture> findById(Integer id);
}
