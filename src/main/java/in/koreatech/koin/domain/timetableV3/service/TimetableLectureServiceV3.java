package in.koreatech.koin.domain.timetableV3.service;

import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateGradesMainFrame;
import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateTotalGrades;
import static in.koreatech.koin.domain.timetableV2.validation.TimetableFrameValidate.validateUserAuthorization;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV3.dto.response.TimetableLectureResponseV3;
import in.koreatech.koin.domain.timetableV3.repository.TimetableFrameRepositoryV3;
import in.koreatech.koin.domain.timetableV3.repository.TimetableLectureRepositoryV3;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimetableLectureServiceV3 {

    @PersistenceContext
    private EntityManager entityManager;
    private final TimetableFrameRepositoryV3 timetableFrameRepositoryV3;
    private final TimetableLectureRepositoryV3 timetableLectureRepositoryV3;

    public TimetableLectureResponseV3 getTimetableLecture(Integer timetableFrameId, Integer userId) {
        TimetableFrame frame = timetableFrameRepositoryV3.getById(timetableFrameId);
        validateUserAuthorization(frame.getUser().getId(), userId);
        return getTimetableLectureResponse(userId, frame);
    }

    private TimetableLectureResponseV3 getTimetableLectureResponse(Integer userId, TimetableFrame timetableFrame) {
        int grades = calculateGradesMainFrame(timetableFrame);
        int totalGrades = calculateTotalGrades(timetableFrameRepositoryV3.findByUserIdAndIsMainTrue(userId));
        return TimetableLectureResponseV3.of(timetableFrame, grades, totalGrades);
    }

    @Transactional
    public TimetableLectureResponseV3 rollbackTimetableLecture(List<Integer> timetableLecturesId, Integer userId) {
        timetableLecturesId.stream()
            .map(timetableLectureRepositoryV3::getByIdWithDeleted)
            .peek(lecture -> validateUserAuthorization(lecture.getTimetableFrame().getUser().getId(), userId))
            .forEach(TimetableLecture::undelete);
        entityManager.flush();
        TimetableLecture timeTableLecture = timetableLectureRepositoryV3.getById(timetableLecturesId.get(0));
        return getTimetableLectureResponse(userId, timeTableLecture.getTimetableFrame());
    }
}
