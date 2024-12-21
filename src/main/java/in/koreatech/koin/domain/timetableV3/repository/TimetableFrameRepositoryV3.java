package in.koreatech.koin.domain.timetableV3.repository;

import org.springframework.data.repository.Repository;

import java.util.List;

import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;

public interface TimetableFrameRepositoryV3 extends Repository<TimetableFrame, Integer> {

    List<TimetableFrame> findByUserIdAndIsMainTrue(Integer userId);
}
