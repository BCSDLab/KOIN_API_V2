package in.koreatech.koin.domain.timetableV3.service;

import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateGradesMainFrame;
import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateTotalGrades;
import static in.koreatech.koin.domain.timetableV2.validation.TimetableFrameValidate.validateUserAuthorization;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.graduation.model.Catalog;
import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.graduation.repository.CatalogRepository;
import in.koreatech.koin.domain.graduation.repository.CourseTypeRepository;
import in.koreatech.koin.domain.student.model.Department;
import in.koreatech.koin.domain.student.model.Major;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV3.dto.request.TimetableRegularLectureCreateRequest;
import in.koreatech.koin.domain.timetableV3.dto.request.TimetableRegularLectureUpdateRequest;
import in.koreatech.koin.domain.timetableV3.dto.response.TimetableLectureResponseV3;
import in.koreatech.koin.domain.timetableV3.repository.LectureRepositoryV3;
import in.koreatech.koin.domain.timetableV3.repository.TimetableFrameRepositoryV3;
import in.koreatech.koin.domain.timetableV3.repository.TimetableLectureRepositoryV3;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableRegularLectureServiceV3 {

    private final TimetableLectureRepositoryV3 timetableLectureRepositoryV3;
    private final TimetableFrameRepositoryV3 timetableFrameRepositoryV3;
    private final LectureRepositoryV3 lectureRepositoryV3;
    private final CatalogRepository catalogRepository;
    private final StudentRepository studentRepository;
    private final CourseTypeRepository courseTypeRepository;

    @Transactional
    public TimetableLectureResponseV3 createTimetablesRegularLecture(
        TimetableRegularLectureCreateRequest request, Integer userId
    ) {
        TimetableFrame frame = timetableFrameRepositoryV3.getById(request.timetableFrameId());
        validateUserAuthorization(frame.getUser().getId(), userId);
        Lecture lecture = lectureRepositoryV3.getById(request.lectureId());
        CourseType courseType = getCourseType(frame.getUser().getId(), lecture);
        TimetableLecture timetableLecture = request.toTimetableLecture(frame, lecture, courseType);
        frame.addTimeTableLecture(timetableLecture);
        timetableLectureRepositoryV3.save(timetableLecture);
        return getTimetableLectureResponse(userId, frame);
    }

    private CourseType getCourseType(Integer userId, Lecture lecture) {
        Student student = studentRepository.getById(userId);
        Department department = student.getDepartment();
        if (Objects.isNull(department)) {
            return null;
        }
        Major major = student.getMajor();
        List<Catalog> catalogs = catalogRepository.findAllByCode(lecture.getCode());
        if (catalogs.isEmpty()) {
            return null;
        }

        if (!Objects.isNull(major)) {
            for (Catalog catalog : catalogs) {
                if (Objects.equals(catalog.getMajor(), major)) {
                    return catalog.getCourseType();
                }
            }
        }

        for (Catalog catalog : catalogs) {
            if (Objects.equals(catalog.getDepartment(), department)) {
                return catalog.getCourseType();
            }
        }

        return null;
    }

    @Transactional
    public TimetableLectureResponseV3 updateTimetablesRegularLecture(
        TimetableRegularLectureUpdateRequest request, Integer userId
    ) {
        TimetableFrame frame = timetableFrameRepositoryV3.getById(request.timetableFrameId());
        validateUserAuthorization(frame.getUser().getId(), userId);

        CourseType courseType = null;
        if (!Objects.isNull(request.timetableLecture().courseType())) {
            courseType = courseTypeRepository.getByName(request.timetableLecture().courseType());
        }

        TimetableLecture timetableLecture = timetableLectureRepositoryV3.getById(request.timetableLecture().id());
        timetableLecture.updateRegularLecture(
            request.timetableLecture().classTitle(),
            request.timetableLecture().classPlacesToString(),
            courseType
        );

        timetableLectureRepositoryV3.save(timetableLecture);
        return getTimetableLectureResponse(userId, frame);
    }

    private TimetableLectureResponseV3 getTimetableLectureResponse(Integer userId, TimetableFrame timetableFrame) {
        int grades = calculateGradesMainFrame(timetableFrame);
        int totalGrades = calculateTotalGrades(timetableFrameRepositoryV3.findByUserIdAndIsMainTrue(userId));
        return TimetableLectureResponseV3.of(timetableFrame, grades, totalGrades);
    }
}
