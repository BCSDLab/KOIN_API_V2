package in.koreatech.koin.domain.timetableV2.service;

import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateGrades;
import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateTotalGrades;
import static in.koreatech.koin.domain.timetableV2.validation.TimetableFrameValidate.validateUserAuthorization;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureCreateRequest;
import in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureUpdateRequest;
import in.koreatech.koin.domain.timetableV2.dto.response.TimetableLectureResponse;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV2.repository.LectureRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableLectureRepositoryV2;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableLectureServiceV2 {
    private final LectureRepositoryV2 lectureRepositoryV2;
    private final TimetableLectureRepositoryV2 timetableLectureRepositoryV2;
    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;

    @Transactional
    public TimetableLectureResponse createTimetableLectures(Integer userId, TimetableLectureCreateRequest request) {
        TimetableFrame timetableFrame = timetableFrameRepositoryV2.getById(request.timetableFrameId());
        validateUserAuthorization(timetableFrame.getUser().getId(), userId);

        for (TimetableLectureCreateRequest.InnerTimeTableLectureRequest timetableLectureRequest : request.timetableLecture()) {
            Lecture lecture = timetableLectureRequest.lectureId() == null ?
                null : lectureRepositoryV2.getLectureById(timetableLectureRequest.lectureId());
            TimetableLecture timetableLecture = timetableLectureRequest.toTimetableLecture(timetableFrame, lecture);
            timetableLectureRepositoryV2.save(timetableLecture);
        }

        return getTimetableLectureResponse(userId, timetableFrame);
    }

    @Transactional
    public TimetableLectureResponse updateTimetablesLectures(Integer userId, TimetableLectureUpdateRequest request) {
        TimetableFrame timetableFrame = timetableFrameRepositoryV2.getById(request.timetableFrameId());
        validateUserAuthorization(timetableFrame.getUser().getId(), userId);

        for (TimetableLectureUpdateRequest.InnerTimetableLectureRequest timetableRequest : request.timetableLecture()) {
            TimetableLecture timetableLecture = timetableLectureRepositoryV2.getById(timetableRequest.id());
            timetableLecture.update(
                timetableRequest.classTitle(),
                timetableRequest.classTime().toString(),
                timetableRequest.classPlace(),
                timetableRequest.professor(),
                timetableRequest.grades(),
                timetableRequest.memo()
            );
        }
        return getTimetableLectureResponse(userId, timetableFrame);
    }

    public TimetableLectureResponse getTimetableLectures(Integer userId, Integer timetableFrameId) {
        TimetableFrame timetableFrame = timetableFrameRepositoryV2.getById(timetableFrameId);
        validateUserAuthorization(timetableFrame.getUser().getId(), userId);
        return getTimetableLectureResponse(userId, timetableFrame);
    }

    @Transactional
    public void deleteTimetableLecture(Integer userId, Integer timetableLectureId) {
        TimetableLecture timetableLecture = timetableLectureRepositoryV2.getById(timetableLectureId);
        TimetableFrame timetableFrame = timetableLecture.getTimetableFrame();
        validateUserAuthorization(timetableFrame.getUser().getId(), userId);
        timetableLectureRepositoryV2.deleteById(timetableLectureId);
    }

    private TimetableLectureResponse getTimetableLectureResponse(Integer userId, TimetableFrame timetableFrame) {
        int grades = calculateGrades(timetableFrame);
        int totalGrades = calculateTotalGrades(timetableFrameRepositoryV2.findByUserIdAndIsMainTrue(userId));
        return TimetableLectureResponse.of(timetableFrame, grades, totalGrades);
    }

    @Transactional
    public void deleteTimetableLectures(List<Integer> request, Integer userId) {
        request.stream()
            .map(timetableLectureRepositoryV2::getById)
            .peek(lecture -> validateUserAuthorization(lecture.getTimetableFrame().getId(), userId))
            .forEach(lecture -> timetableLectureRepositoryV2.deleteById(lecture.getId()));
    }

    @Transactional
    public void deleteTimetableLectureByFrameId(Integer frameId, Integer lectureId, Integer userId) {
        TimetableFrame frame = timetableFrameRepositoryV2.getById(frameId);
        validateUserAuthorization(frame.getUser().getId(), userId);
        timetableLectureRepositoryV2.deleteByFrameIdAndLectureId(frameId, lectureId);
    }
}
