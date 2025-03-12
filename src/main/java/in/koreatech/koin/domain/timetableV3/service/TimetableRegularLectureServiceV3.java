package in.koreatech.koin.domain.timetableV3.service;

import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateGradesMainFrame;
import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateTotalGrades;
import static in.koreatech.koin.domain.timetableV2.validation.TimetableFrameValidate.validateUserOwnsFrame;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.graduation.model.Catalog;
import in.koreatech.koin.domain.graduation.model.CourseType;
import in.koreatech.koin.domain.graduation.model.GeneralEducationArea;
import in.koreatech.koin.domain.graduation.repository.CatalogRepository;
import in.koreatech.koin.domain.graduation.repository.CourseTypeRepository;
import in.koreatech.koin.domain.graduation.repository.GeneralEducationAreaRepository;
import in.koreatech.koin.domain.student.model.Student;
import in.koreatech.koin.domain.student.repository.StudentRepository;
import in.koreatech.koin.domain.student.util.StudentUtil;
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
    private final GeneralEducationAreaRepository generalEducationAreaRepository;

    @Transactional
    public TimetableLectureResponseV3 createTimetablesRegularLecture(
        TimetableRegularLectureCreateRequest request, Integer userId
    ) {
        TimetableFrame frame = timetableFrameRepositoryV3.getById(request.timetableFrameId());
        validateUserOwnsFrame(frame.getUser().getId(), userId);
        Lecture lecture = lectureRepositoryV3.getById(request.lectureId());
        Catalog catalog = getCatalog(lecture, userId);
        CourseType courseType = getCourseType(catalog);
        GeneralEducationArea generalEducationArea = getGeneralEducationArea(catalog);
        TimetableLecture timetableLecture = request.toTimetableLecture(frame, lecture, courseType, generalEducationArea);
        frame.addTimeTableLecture(timetableLecture);
        timetableLectureRepositoryV3.save(timetableLecture);
        return getTimetableLectureResponse(userId, frame);
    }

    private Catalog getCatalog(Lecture lecture, Integer userId) {
        if (lecture == null) {
            return null;
        }
        Student student = studentRepository.getById(userId);
        if (student.getStudentNumber() == null) {
            return null;
        }
        Integer studentNumberYear = StudentUtil.parseStudentNumberYear(student.getStudentNumber());

        List<Catalog> catalogs = catalogRepository.findByLectureNameAndYear(lecture.getName(),
            String.valueOf(studentNumberYear));
        if (!catalogs.isEmpty()) {
            return catalogs.get(0);
        }

        final int currentYear = LocalDateTime.now().getYear();
        for (int initStudentNumberYear = 2019; initStudentNumberYear <= currentYear; initStudentNumberYear++) {
            catalogs = catalogRepository.findByYearAndCode(String.valueOf(initStudentNumberYear),
                lecture.getCode());

            if (!catalogs.isEmpty()) {
                return catalogs.get(0);
            }
        }

        return null;
    }

    private CourseType getCourseType(Catalog catalog) {
        if (catalog != null) {
            return catalog.getCourseType();
        }
        return courseTypeRepository.getByName("이수구분선택");
    }

    private GeneralEducationArea getGeneralEducationArea(Catalog catalog) {
        if (catalog != null) {
            if (catalog.getGeneralEducationArea() != null) {
                return catalog.getGeneralEducationArea();
            }
        }
        return null;
    }

    @Transactional
    public TimetableLectureResponseV3 updateTimetablesRegularLecture(
        TimetableRegularLectureUpdateRequest request, Integer userId
    ) {
        TimetableFrame frame = timetableFrameRepositoryV3.getById(request.timetableFrameId());
        validateUserOwnsFrame(frame.getUser().getId(), userId);

        CourseType courseType = null;
        if (!Objects.isNull(request.timetableLecture().courseType())) {
            courseType = courseTypeRepository.getByName(request.timetableLecture().courseType());
        }

        GeneralEducationArea generalEducationArea = null;
        if (!Objects.isNull(request.timetableLecture().generalEducationArea())) {
            generalEducationArea = generalEducationAreaRepository.getGeneralEducationAreaByName(
                request.timetableLecture().generalEducationArea());
        }

        TimetableLecture timetableLecture = timetableLectureRepositoryV3.getById(request.timetableLecture().id());
        timetableLecture.updateRegularLecture(
            request.timetableLecture().classTitle(),
            request.timetableLecture().classPlacesToString(),
            courseType,
            generalEducationArea
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
