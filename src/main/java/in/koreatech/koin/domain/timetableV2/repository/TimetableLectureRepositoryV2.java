package in.koreatech.koin.domain.timetableV2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.timetableV2.exception.TimetableLectureNotFoundException;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;

public interface TimetableLectureRepositoryV2 extends Repository<TimetableLecture, Integer> {

    Optional<TimetableLecture> findById(Integer id);

    List<TimetableLecture> findAllByTimetableFrameId(Integer frameId);

    void deleteById(Integer id);

    default TimetableLecture getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> TimetableLectureNotFoundException.withDetail("id: " + id));
    }

    TimetableLecture save(TimetableLecture timetableLecture);

    Optional<TimetableLecture> findByTimetableFrameIdAndLectureId(Integer frameId, Integer lectureId);

    default TimetableLecture getByFrameIdAndLectureId(Integer frameId, Integer lectureId) {
        return findByTimetableFrameIdAndLectureId(frameId, lectureId)
            .orElseThrow(() -> TimetableLectureNotFoundException.withDetail("frameId: " + frameId + ", lectureId: " + lectureId));
    }

    @Query(value = "SELECT * FROM timetable_lecture WHERE id = :id", nativeQuery = true)
    Optional<TimetableLecture> findByIdWithDeleted(@Param("id") Integer id);

    default TimetableLecture getByIdWithDeleted(Integer id) {
        return findByIdWithDeleted(id)
            .orElseThrow(() -> TimetableLectureNotFoundException.withDetail("id: " + id));
    }
}
