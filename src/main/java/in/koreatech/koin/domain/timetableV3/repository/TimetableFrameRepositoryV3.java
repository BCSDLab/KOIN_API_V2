package in.koreatech.koin.domain.timetableV3.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetable.exception.TimetableNotFoundException;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;

public interface TimetableFrameRepositoryV3 extends Repository<TimetableFrame, Integer> {

    Optional<TimetableFrame> findById(Integer id);

    default TimetableFrame getById(Integer id) {
        return findById(id)
            .orElseThrow(() -> TimetableNotFoundException.withDetail("id: " + id));
    }

    List<TimetableFrame> findByUserIdAndIsMainTrue(Integer userId);
}
