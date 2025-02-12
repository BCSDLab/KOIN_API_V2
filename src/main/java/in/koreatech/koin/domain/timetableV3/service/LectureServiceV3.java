package in.koreatech.koin.domain.timetableV3.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.graduation.model.Catalog;
import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.graduation.repository.CatalogRepository;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.student.util.StudentUtil;
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
    private final StudentRepository studentRepository;

    public List<LectureResponseV3> getLectures(Integer year, String term, Integer userId) {
        Semester semester = semesterRepositoryV3.getByYearAndTerm(year, Term.fromDescription(term));
        List<Lecture> lectures = lectureRepositoryV3.findBySemester(semester.getSemester());

        return lectures.stream()
            .map(lecture -> {
                if (Objects.isNull(userId)) {
                    return LectureResponseV3.of(lecture, null);
                }
                return LectureResponseV3.of(lecture, getCourseType(lecture, userId));
            })
            .toList();
    }

    private CourseType getCourseType(Lecture lecture, Integer userId) {
        Student student = studentRepository.getById(userId);
        Integer studentNumberYear = StudentUtil.parseStudentNumberYear(student.getStudentNumber());

        Catalog catalog = catalogRepository.findByYearAndCodeAndLectureName(String.valueOf(studentNumberYear),
            lecture.getCode(), lecture.getName()).orElse(null);
        if (!Objects.isNull(catalog)) {
            return catalog.getCourseType();
        }

        final int currentYear = LocalDateTime.now().getYear();
        for (int initStudentNumberYear = 2019; initStudentNumberYear <= currentYear; initStudentNumberYear++) {
            catalog = catalogRepository.findByYearAndCodeAndLectureName(String.valueOf(studentNumberYear),
                lecture.getCode(), lecture.getName()).orElse(null);
            if (!Objects.isNull(catalog)) {
                return catalog.getCourseType();
            }
        }

        return null;
    }
}
