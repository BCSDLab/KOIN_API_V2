package in.koreatech.koin.domain.timetableV2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetableV2.exception.TimetableLectureNotFoundException;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;

public interface TimetableLectureRepositoryV2 extends Repository<TimetableLecture, Integer> {

    Optional<TimetableLecture> findById(Integer id);

    List<TimetableLecture> findAllByTimetableFrameId(Integer id);

    void deleteById(Integer id);

    default TimetableLecture getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> TimetableLectureNotFoundException.withDetail("id: " + id));
    }
    TimetableLecture save(TimetableLecture timetableLecture);
}
