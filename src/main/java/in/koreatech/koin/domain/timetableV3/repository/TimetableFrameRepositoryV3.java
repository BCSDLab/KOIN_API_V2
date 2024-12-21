package in.koreatech.koin.domain.timetableV3.repository;

import org.springframework.data.repository.Repository;

import java.util.List;

import in.koreatech.koin.domain.timetableV3.model.TimetableFrameV3;

public interface TimetableFrameRepositoryV3 extends Repository<TimetableFrameV3, Integer> {

    List<TimetableFrameV3> findByUserIdAndIsMainTrue(Integer userId);
}
