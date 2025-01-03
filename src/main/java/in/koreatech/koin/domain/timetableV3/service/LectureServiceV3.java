package in.koreatech.koin.domain.timetableV3.service;

import java.util.List;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.timetableV3.dto.response.LectureResponseV3;
import in.koreatech.koin.domain.timetableV3.repository.LectureRepositoryV3;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LectureServiceV3 {

    private final LectureRepositoryV3 lectureRepositoryV3;

    public List<LectureResponseV3> getLectures(String semesterDate) {
        return lectureRepositoryV3.findBySemester(semesterDate).stream()
            .map(LectureResponseV3::from)
            .toList();
    }
}
