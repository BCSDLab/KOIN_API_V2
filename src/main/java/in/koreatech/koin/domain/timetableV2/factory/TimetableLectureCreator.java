package in.koreatech.koin.domain.timetableV2.factory;

import static in.koreatech.koin.domain.student.util.StudentUtil.parseStudentNumberYear;
import static in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureCreateRequest.InnerTimeTableLectureRequest;

import java.util.Objects;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.graduation.model.Catalog;
import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.graduation.repository.CatalogRepository;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureCreateRequest;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
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

    public void createTimetableLectures(TimetableLectureCreateRequest request, TimetableFrame frame) {
        for (InnerTimeTableLectureRequest lectureRequest : request.timetableLecture()) {
            Lecture lecture = determineLecture(lectureRequest.lectureId());
            CourseType courseType = getCourseType(frame.getUser().getId(), lecture);
            TimetableLecture timetableLecture = lectureRequest.toTimetableLecture(frame, lecture, courseType);
            frame.addTimeTableLecture(timetableLecture);
            timetableLectureRepositoryV2.save(timetableLecture);
        }
    }

    private Lecture determineLecture(Integer lectureId) {
        if (lectureId != null) {
            return lectureRepositoryV2.getLectureById(lectureId);
        }
        return null;
    }

    private CourseType getCourseType(Integer userId, Lecture lecture) {
        if (lecture == null) {
            return null;
        }
        Student student = studentRepository.getById(userId);
        if (Objects.isNull(student.getDepartment())) {
            return null;
        }
        String year = parseStudentNumberYear(student.getStudentNumber()).toString();
        String code = lecture.getCode();
        Catalog catalog = catalogRepository.getByYearAndDepartmentAndCode(year, student.getDepartment(), code);
        return catalog.getCourseType();
    }
}
