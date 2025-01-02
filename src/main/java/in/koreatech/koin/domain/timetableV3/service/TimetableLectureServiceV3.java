package in.koreatech.koin.domain.timetableV3.service;

import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateGradesMainFrame;
import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateTotalGrades;
import static in.koreatech.koin.domain.timetableV2.validation.TimetableFrameValidate.validateUserAuthorization;

import org.springframework.stereotype.Service;

import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV3.dto.response.TimetableLectureResponseV3;
import in.koreatech.koin.domain.timetableV3.repository.TimetableFrameRepositoryV3;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TimetableLectureServiceV3 {

    private final TimetableFrameRepositoryV3 timetableFrameRepositoryV3;

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
}
