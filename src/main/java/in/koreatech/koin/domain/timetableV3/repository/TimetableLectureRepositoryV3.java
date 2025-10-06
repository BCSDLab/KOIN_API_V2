package in.koreatech.koin.domain.timetableV3.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.timetableV2.exception.TimetableLectureNotFoundException;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.config.repository.JpaRepositoryMarker;

@JpaRepositoryMarker
public interface TimetableLectureRepositoryV3 extends Repository<TimetableLecture, Integer> {
    TimetableLecture save(TimetableLecture timetableLecture);

    Optional<TimetableLecture> findById(Integer id);

    default TimetableLecture getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> TimetableLectureNotFoundException.withDetail("id: " + id));
    }

    @Query(value = "SELECT * FROM timetable_lecture WHERE id = :id", nativeQuery = true)
    Optional<TimetableLecture> findByIdWithDeleted(@Param("id") Integer id);

    default TimetableLecture getByIdWithDeleted(Integer id) {
        return findByIdWithDeleted(id)
            .orElseThrow(() -> TimetableLectureNotFoundException.withDetail("id: " + id));
    }

    @Query(value = "SELECT * FROM timetable_lecture WHERE frame_id = :frameId", nativeQuery = true)
    List<TimetableLecture> findAllByFrameIdWithDeleted(@Param("frameId") Integer frameId);
}
