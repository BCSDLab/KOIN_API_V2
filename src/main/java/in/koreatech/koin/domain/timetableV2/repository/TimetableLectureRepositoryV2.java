package in.koreatech.koin.domain.timetableV2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import in.koreatech.koin.domain.timetableV2.exception.TimetableLectureNotFoundException;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;

public interface TimetableLectureRepositoryV2 extends Repository<TimetableLecture, Integer> {

    Optional<TimetableLecture> findById(Integer id);

    List<TimetableLecture> findAllByTimetableFrameId(Integer frameId);

    void saveAll(Iterable<TimetableLecture> timetableLectures);

    void deleteById(Integer id);

    default TimetableLecture getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> TimetableLectureNotFoundException.withDetail("id: " + id));
    }

    TimetableLecture save(TimetableLecture timetableLecture);

    List<TimetableLecture> findAllByTimetableFrameIdAndLectureId(Integer frameId, Integer lectureId);

    default List<TimetableLecture> getAllByFrameIdAndLectureId(Integer frameId, Integer lectureId) {
        List<TimetableLecture> timetableLectures = findAllByTimetableFrameIdAndLectureId(frameId, lectureId);

        if (timetableLectures.isEmpty()) {
            throw TimetableLectureNotFoundException.withDetail(
                "frameId: " + frameId + ", lectureId: " + lectureId);
        }

        return timetableLectures;
    }

    @Query(value = "SELECT * FROM timetable_lecture WHERE id = :id", nativeQuery = true)
    Optional<TimetableLecture> findByIdWithDeleted(@Param("id") Integer id);

    default TimetableLecture getByIdWithDeleted(Integer id) {
        return findByIdWithDeleted(id)
            .orElseThrow(() -> TimetableLectureNotFoundException.withDetail("id: " + id));
    }

    @Query(value = "SELECT * FROM timetable_lecture WHERE frame_id = :frameId", nativeQuery = true)
    List<TimetableLecture> findAllByFrameIdWithDeleted(@Param("frameId") Integer frameId);

    @Query(value = """
        SELECT DISTINCT s.year FROM timetable_lecture tl
        JOIN timetable_frame tf ON tl.frame_id = tf.id
        JOIN semester s ON tf.semester_id = s.id
        WHERE tf.user_id = :userId
        """, nativeQuery = true)
    List<String> findYearsByUserId(@Param("userId") Integer userId);
}
