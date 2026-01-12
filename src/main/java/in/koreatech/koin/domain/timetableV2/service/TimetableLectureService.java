package in.koreatech.koin.domain.timetableV2.service;

import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateGradesMainFrame;
import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateTotalGrades;
import static in.koreatech.koin.domain.timetableV2.validation.TimetableFrameValidate.validateUserOwnsFrame;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.global.concurrent.ConcurrencyGuard;
import in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureCreateRequest;
import in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureUpdateRequest;
import in.koreatech.koin.domain.timetableV2.dto.response.TimetableLectureResponse;
import in.koreatech.koin.domain.timetableV2.factory.TimetableLectureCreator;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableLectureRepositoryV2;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableLectureService {

    @PersistenceContext
    private EntityManager entityManager;
    private final UserRepository userRepository;
    private final TimetableLectureRepositoryV2 timetableLectureRepositoryV2;
    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;
    private final TimetableLectureCreator timetableLectureCreator;

    @Transactional
    @ConcurrencyGuard(lockName = "createTimetableLecture")
    public TimetableLectureResponse createTimetableLectures(Integer userId, TimetableLectureCreateRequest request) {
        TimetableFrame frame = timetableFrameRepositoryV2.getById(request.timetableFrameId());
        validateUserOwnsFrame(frame.getUser().getId(), userId);
        timetableLectureCreator.createTimetableLectures(request, frame, userId);
        return getTimetableLectureResponse(userId, frame);
    }

    @Transactional
    public TimetableLectureResponse updateTimetablesLectures(Integer userId, TimetableLectureUpdateRequest request) {
        TimetableFrame frame = timetableFrameRepositoryV2.getById(request.timetableFrameId());
        validateUserOwnsFrame(frame.getUser().getId(), userId);
        for (TimetableLectureUpdateRequest.InnerTimetableLectureRequest timetableRequest : request.timetableLecture()) {
            TimetableLecture timetableLecture = timetableLectureRepositoryV2.getById(timetableRequest.id());
            timetableLecture.update(
                timetableRequest.classTitle(),
                timetableRequest.getClassTimeToString(),
                timetableRequest.getClassPlaceToString(),
                timetableRequest.professor(),
                timetableRequest.grades(),
                timetableRequest.memo()
            );
        }
        return getTimetableLectureResponse(userId, frame);
    }

    public TimetableLectureResponse getTimetableLectures(Integer userId, Integer timetableFrameId) {
        TimetableFrame timetableFrame = timetableFrameRepositoryV2.getById(timetableFrameId);
        validateUserOwnsFrame(timetableFrame.getUser().getId(), userId);
        return getTimetableLectureResponse(userId, timetableFrame);
    }

    @Transactional
    public void deleteTimetableLecture(Integer userId, Integer timetableLectureId) {
        TimetableLecture timetableLecture = timetableLectureRepositoryV2.getById(timetableLectureId);
        TimetableFrame timetableFrame = timetableLecture.getTimetableFrame();
        validateUserOwnsFrame(timetableFrame.getUser().getId(), userId);
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
            .peek(lecture -> validateUserOwnsFrame(lecture.getTimetableFrame().getUser().getId(), userId))
            .forEach(TimetableLecture::delete);
    }

    @Transactional
    public void deleteTimetableLectureByFrameId(Integer frameId, Integer lectureId, Integer userId) {
        TimetableFrame frame = timetableFrameRepositoryV2.getById(frameId);
        validateUserOwnsFrame(frame.getUser().getId(), userId);
        List<TimetableLecture> timetableLectures = timetableLectureRepositoryV2
            .getAllByFrameIdAndLectureId(frameId, lectureId);
        timetableLectures.forEach(TimetableLecture::delete);
    }

    @Transactional
    public TimetableLectureResponse rollbackTimetableLecture(List<Integer> timetableLecturesId, Integer userId) {
        timetableLecturesId.stream()
            .map(timetableLectureRepositoryV2::getByIdWithDeleted)
            .peek(lecture -> validateUserOwnsFrame(lecture.getTimetableFrame().getUser().getId(), userId))
            .forEach(TimetableLecture::undelete);
        entityManager.flush();
        TimetableLecture timeTableLecture = timetableLectureRepositoryV2.getById(timetableLecturesId.get(0));
        return getTimetableLectureResponse(userId, timeTableLecture.getTimetableFrame());
    }

    @Transactional
    public TimetableLectureResponse rollbackTimetableFrame(Integer frameId, Integer userId) {
        TimetableFrame timetableFrame = timetableFrameRepositoryV2.getByIdWithDeleted(frameId);
        validateUserOwnsFrame(timetableFrame.getUser().getId(), userId);

        User user = userRepository.getById(userId);
        boolean hasTimetableFrame = timetableFrameRepositoryV2.existsByUserAndSemester(user,
            timetableFrame.getSemester());

        if (!hasTimetableFrame) {
            timetableFrame.setMain(true);
        }
        timetableFrame.undelete();

        timetableLectureRepositoryV2.findAllByFrameIdWithDeleted(timetableFrame.getId()).stream()
            .map(TimetableLecture::getId)
            .map(timetableLectureRepositoryV2::getByIdWithDeleted)
            .forEach(TimetableLecture::undelete);
        entityManager.flush();
        return getTimetableLectureResponse(userId, timetableFrame);
    }
}
