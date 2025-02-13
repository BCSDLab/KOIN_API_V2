package in.koreatech.koin.domain.timetableV2.factory;

import static in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureCreateRequest.InnerTimeTableLectureRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import in.koreatech.koin.domain.graduation.model.Catalog;
import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.graduation.repository.CatalogRepository;
import in.koreatech.koin.domain.graduation.repository.CourseTypeRepository;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.student.util.StudentUtil;
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
    private final CourseTypeRepository courseTypeRepository;

    public void createTimetableLectures(TimetableLectureCreateRequest request, TimetableFrame frame) {
        for (InnerTimeTableLectureRequest lectureRequest : request.timetableLecture()) {
            Lecture lecture = determineLecture(lectureRequest.lectureId());
            CourseType courseType = getCourseType(lecture, frame.getUser().getId());
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

    private CourseType getCourseType(Lecture lecture, Integer userId) {
        Student student = studentRepository.getById(userId);
        Integer studentNumberYear = StudentUtil.parseStudentNumberYear(student.getStudentNumber());

        List<Catalog> catalogs = catalogRepository.findByLectureNameAndYear(lecture.getName(),
            String.valueOf(studentNumberYear));
        if (!catalogs.isEmpty()) {
            return catalogs.get(0).getCourseType();
        }

        final int currentYear = LocalDateTime.now().getYear();
        for (int initStudentNumberYear = 2019; initStudentNumberYear <= currentYear; initStudentNumberYear++) {
            catalogs = catalogRepository.findByYearAndCode(String.valueOf(initStudentNumberYear),
                lecture.getCode());

            if (!Objects.isNull(catalogs)) {
                return catalogs.get(0).getCourseType();
            }
        }

        return courseTypeRepository.getByName("이수구분선택");
    }
}
