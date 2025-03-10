package in.koreatech.koin.domain.timetableV3.service;

import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateGradesMainFrame;
import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateTotalGrades;
import static in.koreatech.koin.domain.timetableV2.validation.TimetableFrameValidate.validateUserOwnsFrame;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV3.dto.response.TakeAllTimetableLectureResponse;
import in.koreatech.koin.domain.timetableV3.dto.response.TimetableLectureResponseV3;
import in.koreatech.koin.domain.timetableV3.repository.TimetableFrameRepositoryV3;
import in.koreatech.koin.domain.timetableV3.repository.TimetableLectureRepositoryV3;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableLectureServiceV3 {

    @PersistenceContext
    private EntityManager entityManager;
    private final TimetableFrameRepositoryV3 timetableFrameRepositoryV3;
    private final TimetableLectureRepositoryV3 timetableLectureRepositoryV3;
    private final UserRepository userRepository;

    public TimetableLectureResponseV3 getTimetableLecture(Integer timetableFrameId, Integer userId) {
        TimetableFrame frame = timetableFrameRepositoryV3.getById(timetableFrameId);
        validateUserOwnsFrame(frame.getUser().getId(), userId);
        return getTimetableLectureResponse(userId, frame);
    }

    private TimetableLectureResponseV3 getTimetableLectureResponse(Integer userId, TimetableFrame timetableFrame) {
        int grades = calculateGradesMainFrame(timetableFrame);
        int totalGrades = calculateTotalGrades(timetableFrameRepositoryV3.findByUserIdAndIsMainTrue(userId));
        return TimetableLectureResponseV3.of(timetableFrame, grades, totalGrades);
    }

    public TakeAllTimetableLectureResponse getTakeAllTimetableLectures(Integer userId) {
        List<TimetableFrame> frames = timetableFrameRepositoryV3.findAllByUserIdAndIsMainTrue(userId);

        List<TimetableLecture> collectedLectures = frames.stream()
            .flatMap(frame -> frame.getTimetableLectures().stream())
            .toList();

        return new TakeAllTimetableLectureResponse(
            TakeAllTimetableLectureResponse.InnerTimetableLectureResponseV3.from(collectedLectures)
        );
    }

    @Transactional
    public TimetableLectureResponseV3 rollbackTimetableLecture(List<Integer> timetableLecturesId, Integer userId) {
        timetableLecturesId.stream()
            .map(timetableLectureRepositoryV3::getByIdWithDeleted)
            .peek(lecture -> validateUserOwnsFrame(lecture.getTimetableFrame().getUser().getId(), userId))
            .forEach(TimetableLecture::undelete);
        entityManager.flush();
        TimetableLecture timeTableLecture = timetableLectureRepositoryV3.getById(timetableLecturesId.get(0));
        return getTimetableLectureResponse(userId, timeTableLecture.getTimetableFrame());
    }

    @Transactional
    public TimetableLectureResponseV3 rollbackTimetableFrame(Integer frameId, Integer userId) {
        TimetableFrame timetableFrame = timetableFrameRepositoryV3.getByIdWithDeleted(frameId);
        validateUserOwnsFrame(timetableFrame.getUser().getId(), userId);

        User user = userRepository.getById(userId);
        boolean hasTimetableFrame = timetableFrameRepositoryV3.existsByUserAndSemester(user,
            timetableFrame.getSemester());

        if (!hasTimetableFrame) {
            timetableFrame.setMain(true);
        }
        timetableFrame.undelete();

        timetableLectureRepositoryV3.findAllByFrameIdWithDeleted(timetableFrame.getId()).stream()
            .map(TimetableLecture::getId)
            .map(timetableLectureRepositoryV3::getByIdWithDeleted)
            .forEach(TimetableLecture::undelete);
        entityManager.flush();
        return getTimetableLectureResponse(userId, timetableFrame);
    }
}
