package in.koreatech.koin.domain.timetableV2.service;

import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateGradesMainFrame;
import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateTotalGrades;
import static in.koreatech.koin.domain.timetableV2.validation.TimetableFrameValidate.validateUserAuthorization;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureCreateRequest;
import in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureUpdateRequest;
import in.koreatech.koin.domain.timetableV2.dto.response.TimetableLectureResponse;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableLectureRepositoryV2;
import in.koreatech.koin.domain.timetableV2.factory.TimetableLectureCreator;
import in.koreatech.koin.domain.timetableV2.factory.TimetableLectureUpdater;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableLectureService {

    private final TimetableLectureRepositoryV2 timetableLectureRepositoryV2;
    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;
    private final TimetableLectureCreator timetableLectureCreator;
    private final TimetableLectureUpdater timetableLectureUpdater;

    @Transactional
    public TimetableLectureResponse createTimetableLectures(Integer userId, TimetableLectureCreateRequest request) {
        TimetableFrame frame = timetableFrameRepositoryV2.getById(request.timetableFrameId());
        validateUserAuthorization(frame.getUser().getId(), userId);
        timetableLectureCreator.createTimetableLectures(request, frame);
        return getTimetableLectureResponse(userId, frame);
    }

    @Transactional
    public TimetableLectureResponse updateTimetablesLectures(Integer userId, TimetableLectureUpdateRequest request) {
        TimetableFrame frame = timetableFrameRepositoryV2.getById(request.timetableFrameId());
        validateUserAuthorization(frame.getUser().getId(), userId);
        timetableLectureUpdater.updateTimetablesLectures(request);
        return getTimetableLectureResponse(userId, frame);
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
        timetableLecture.delete();
    }

    private TimetableLectureResponse getTimetableLectureResponse(Integer userId, TimetableFrame timetableFrame) {
        int grades = calculateGradesMainFrame(timetableFrame);
        int totalGrades = calculateTotalGrades(timetableFrameRepositoryV2.findByUserIdAndIsMainTrue(userId));
        return TimetableLectureResponse.of(timetableFrame, grades, totalGrades);
    }

    @Transactional
    public void deleteTimetableLectures(List<Integer> request, Integer userId) {
        request.stream()
            .map(timetableLectureRepositoryV2::getById)
            .peek(lecture -> validateUserAuthorization(lecture.getTimetableFrame().getUser().getId(), userId))
            .forEach(TimetableLecture::delete);
    }

    @Transactional
    public void deleteTimetableLectureByFrameId(Integer frameId, Integer lectureId, Integer userId) {
        TimetableFrame frame = timetableFrameRepositoryV2.getById(frameId);
        validateUserAuthorization(frame.getUser().getId(), userId);
        TimetableLecture timetableLecture = timetableLectureRepositoryV2.getByFrameIdAndLectureId(frameId, lectureId);
        timetableLecture.delete();
    }

    @Transactional
    public TimetableLectureResponse rollbackTimetableLecture(List<Integer> timetableLecturesId, Integer userId) {
        timetableLecturesId.stream()
            .map(timetableLectureRepositoryV2::getByIdWithDeleted)
            .peek(lecture -> validateUserAuthorization(lecture.getTimetableFrame().getUser().getId(), userId))
            .forEach(TimetableLecture::undelete);
        TimetableLecture timeTableLecture = timetableLectureRepositoryV2.getById(timetableLecturesId.get(0));
        return getTimetableLectureResponse(userId, timeTableLecture.getTimetableFrame());
    }

    @Transactional
    public TimetableLectureResponse rollbackTimetableFrame(Integer frameId, Integer userId) {
        TimetableFrame timetableFrame = timetableFrameRepositoryV2.getByIdWithDeleted(frameId);
        validateUserAuthorization(timetableFrame.getUser().getId(), userId);
        timetableFrame.undelete();

        timetableFrame.getTimetableLectures().stream()
            .map(TimetableLecture::getId)
            .map(timetableLectureRepositoryV2::getByIdWithDeleted)
            .forEach(TimetableLecture::undelete);

        return getTimetableLectureResponse(userId, timetableFrame);
    }
}
