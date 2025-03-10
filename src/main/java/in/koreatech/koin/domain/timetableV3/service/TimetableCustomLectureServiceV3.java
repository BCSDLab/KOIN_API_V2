package in.koreatech.koin.domain.timetableV3.service;

import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateGradesMainFrame;
import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateTotalGrades;
import static in.koreatech.koin.domain.timetableV2.validation.TimetableFrameValidate.validateUserOwnsFrame;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV3.dto.request.TimetableCustomLectureCreateRequest;
import in.koreatech.koin.domain.timetableV3.dto.request.TimetableCustomLectureUpdateRequest;
import in.koreatech.koin.domain.timetableV3.dto.response.TimetableLectureResponseV3;
import in.koreatech.koin.domain.timetableV3.repository.TimetableFrameRepositoryV3;
import in.koreatech.koin.domain.timetableV3.repository.TimetableLectureRepositoryV3;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableCustomLectureServiceV3 {

    private final TimetableFrameRepositoryV3 timetableFrameRepositoryV3;
    private final TimetableLectureRepositoryV3 timetableLectureRepositoryV3;

    @Transactional
    public TimetableLectureResponseV3 createTimetablesCustomLecture(
        TimetableCustomLectureCreateRequest request, Integer userId
    ) {
        TimetableFrame frame = timetableFrameRepositoryV3.getById(request.timetableFrameId());
        validateUserOwnsFrame(frame.getUser().getId(), userId);
        TimetableLecture timetableLecture = request.toTimetableLecture(frame);
        frame.addTimeTableLecture(timetableLecture);
        timetableLectureRepositoryV3.save(timetableLecture);
        return getTimetableLectureResponse(userId, frame);
    }

    @Transactional
    public TimetableLectureResponseV3 updateTimetablesCustomLecture(
        TimetableCustomLectureUpdateRequest request, Integer userId
    ) {
        TimetableFrame frame = timetableFrameRepositoryV3.getById(request.timetableFrameId());
        validateUserOwnsFrame(frame.getUser().getId(), userId);
        TimetableLecture timetableLecture = timetableLectureRepositoryV3.getById(request.timetableLecture().id());

        timetableLecture.updateCustomLecture(
            request.timetableLecture().classTitle(),
            request.timetableLecture().professor(),
            request.timetableLecture().joinClassTimes(),
            request.timetableLecture().joinClassPlaces()
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
