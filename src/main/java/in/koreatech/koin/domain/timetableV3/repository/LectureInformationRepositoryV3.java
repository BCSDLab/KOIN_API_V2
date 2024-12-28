package in.koreatech.koin.domain.timetableV3.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import in.koreatech.koin.domain.timetableV3.model.LectureInformation;

public interface LectureInformationRepositoryV3 extends Repository<LectureInformation, Integer> {
    List<LectureInformation> findByLectureId(Integer lectureId);
}
