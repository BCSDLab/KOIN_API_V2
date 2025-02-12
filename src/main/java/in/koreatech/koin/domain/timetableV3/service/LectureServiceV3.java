package in.koreatech.koin.domain.timetableV3.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.graduation.model.Catalog;
import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.graduation.repository.CatalogRepository;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV3.dto.response.LectureResponseV3;
import in.koreatech.koin.domain.timetableV3.model.Term;
import in.koreatech.koin.domain.timetableV3.repository.LectureRepositoryV3;
import in.koreatech.koin.domain.timetableV3.repository.SemesterRepositoryV3;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LectureServiceV3 {

    private final LectureRepositoryV3 lectureRepositoryV3;
    private final SemesterRepositoryV3 semesterRepositoryV3;
    private final CatalogRepository catalogRepository;

    public List<LectureResponseV3> getLectures(Integer year, String term) {
        Semester semester = semesterRepositoryV3.getByYearAndTerm(year, Term.fromDescription(term));
        List<Lecture> lectures = lectureRepositoryV3.findBySemester(semester.getSemester());

        return lectures.stream()
            .map(lecture -> {
                CourseType courseType = catalogRepository.findByCodeAndYear(lecture.getCode(),
                        String.valueOf(semester.getSemester()))
                    .map(Catalog::getCourseType)
                    .orElse(null);

                return LectureResponseV3.of(lecture, courseType);
            })
            .toList();
    }
}
