package in.koreatech.koin.domain.timetableV3.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV3.dto.response.LectureResponseV3;
import in.koreatech.koin.domain.timetableV3.model.Term;
import in.koreatech.koin.domain.timetableV3.repository.LectureRepositoryV3;
import in.koreatech.koin.domain.timetableV3.repository.SemesterRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LectureServiceV3 {

    private final LectureRepositoryV3 lectureRepositoryV3;
    private final SemesterRepositoryV3 semesterRepositoryV3;

    public List<LectureResponseV3> getLectures(Integer year, String term) {
        Semester semester = semesterRepositoryV3.getByYearAndTerm(year, Term.fromDescription(term));
        return lectureRepositoryV3.findBySemester(semester.getSemester()).stream()
            .map(LectureResponseV3::of)
            .toList();
    }
}
