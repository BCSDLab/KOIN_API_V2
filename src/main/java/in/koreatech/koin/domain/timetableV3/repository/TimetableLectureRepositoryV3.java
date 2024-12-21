package in.koreatech.koin.domain.timetableV3.repository;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;

public interface TimetableLectureRepositoryV3 extends Repository<TimetableLecture, Integer> {
    TimetableLecture save(TimetableLecture timetableLecture);
}
