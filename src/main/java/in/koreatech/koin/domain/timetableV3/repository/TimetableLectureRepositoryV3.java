package in.koreatech.koin.domain.timetableV3.repository;

import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetableV2.exception.TimetableLectureNotFoundException;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;

public interface TimetableLectureRepositoryV3 extends Repository<TimetableLecture, Integer> {
    TimetableLecture save(TimetableLecture timetableLecture);

    Optional<TimetableLecture> findById(Integer id);

    default TimetableLecture getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> TimetableLectureNotFoundException.withDetail("id: " + id));
    }
}
