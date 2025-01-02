package in.koreatech.koin.domain.timetableV2.factory;

import static in.koreatech.koin.domain.student.util.StudentUtil.parseStudentNumberYear;
import static in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureCreateRequest.*;
import static in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureCreateRequest.InnerTimeTableLectureRequest;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.graduation.model.Catalog;
import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.graduation.repository.CatalogRepository;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.student.util.StudentUtil;
import in.koreatech.koin.domain.graduation.model.Catalog;
import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureCreateRequest;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.graduation.repository.CatalogRepository;
import in.koreatech.koin.domain.timetableV2.repository.LectureRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableLectureRepositoryV2;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TimetableLectureCreator {

    private final CatalogRepository catalogRepository;
    private final StudentRepository studentRepository;
    private final LectureRepositoryV2 lectureRepositoryV2;
    private final TimetableLectureRepositoryV2 timetableLectureRepositoryV2;
    private final CatalogRepository catalogRepository;
    private final StudentRepository studentRepository;

    public void createTimetableLectures(TimetableLectureCreateRequest request, Integer userId, TimetableFrame frame) {
        for (InnerTimeTableLectureRequest lectureRequest : request.timetableLecture()) {
            Lecture lecture = determineLecture(lectureRequest.lectureId());
            CourseType courseType = determineCourseType(lecture, userId);
            TimetableLecture timetableLecture = lectureRequest.toTimetableLecture(frame, lecture, courseType);
            frame.addTimeTableLecture(timetableLecture);
            timetableLectureRepositoryV2.save(timetableLecture);
        }
    }

    private CourseType determineCourseType(Lecture lecture, Integer userId) {
        if (lecture != null) {
            return getCourseType(userId, lecture);
        }
        return null;
    }

    private CourseType getCourseType(Integer userId, Lecture lecture) {
        Student student = studentRepository.getById(userId);
        String year = StudentUtil.parseStudentNumberYear(student.getStudentNumber()).toString();
        String code = lecture.getCode();
        Catalog catalog = catalogRepository.getByYearAndDepartmentAndCode(year, student.getDepartment(), code);
        return catalog.getCourseType();
    }

    private Lecture determineLecture(Integer lectureId) {
        if (lectureId != null) {
            return lectureRepositoryV2.getLectureById(lectureId);
        }
        return null;
    }
}
