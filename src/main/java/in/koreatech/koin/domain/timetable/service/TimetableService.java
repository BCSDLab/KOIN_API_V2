package in.koreatech.koin.domain.timetable.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.dto.LectureResponse;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetableV2.repository.LectureRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.SemesterRepositoryV2;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableService {

    private final LectureRepositoryV2 lectureRepositoryV2;
    private final SemesterRepositoryV2 semesterRepositoryV2;

    public List<LectureResponse> getLecturesBySemester(String semester) {
        semesterRepositoryV2.getBySemester(semester);
        List<Lecture> lectures = lectureRepositoryV2.findBySemester(semester);
        return lectures.stream()
            .map(LectureResponse::from)
            .toList();
    }
}
