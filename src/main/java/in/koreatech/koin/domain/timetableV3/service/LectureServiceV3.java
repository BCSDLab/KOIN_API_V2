package in.koreatech.koin.domain.timetableV3.service;

import java.util.List;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV3.dto.response.LectureResponseV3;
import in.koreatech.koin.domain.timetableV3.model.Term;
import in.koreatech.koin.domain.timetableV3.repository.LectureRepositoryV3;
import in.koreatech.koin.domain.timetableV3.repository.SemesterRepositoryV3;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LectureServiceV3 {

    private final LectureRepositoryV3 lectureRepositoryV3;
    private final SemesterRepositoryV3 semesterRepositoryV3;

    public List<LectureResponseV3> getLectures(Integer year, Term term) {
        Semester semester = semesterRepositoryV3.getByYearAndTerm(year, term);
        return lectureRepositoryV3.findBySemester(semester.getSemester()).stream()
            .map(LectureResponseV3::from)
            .toList();
    }
}
