package in.koreatech.koin.domain.timetable.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.exception.TimetableNotFoundException;
import in.koreatech.koin.domain.timetable.model.Timetable;

public interface TimetableRepository extends Repository<Timetable, Integer> {

    Timetable save(Timetable timeTable);

    List<Timetable> findAllByUserId(Integer id);

    List<Timetable> findAllByUserIdAndSemesterId(Integer userId, Integer semesterId);

    Optional<Timetable> findById(Integer id);

    void deleteByUserIdAndSemesterId(Integer userId, Integer semesterId);

    default Timetable getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> TimetableNotFoundException.withDetail("id: " + id));
    }
}
