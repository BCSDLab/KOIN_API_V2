package in.koreatech.koin.domain.timetableV3.service;

import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateGradesMainFrame;
import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateTotalGrades;
import static in.koreatech.koin.domain.timetableV2.validation.TimetableFrameValidate.validateUserAuthorization;
import static in.koreatech.koin.domain.timetableV3.dto.request.TimetableCustomLectureUpdateRequest.InnerTimeTableCustomLectureRequest.LectureInfo;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetableV2.exception.TimetableLectureNotFoundException;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV3.dto.request.TimetableCustomLectureCreateRequest;
import in.koreatech.koin.domain.timetableV3.dto.request.TimetableCustomLectureUpdateRequest;
import in.koreatech.koin.domain.timetableV3.dto.response.TimetableLectureResponseV3;
import in.koreatech.koin.domain.timetableV3.model.TimetableCustomLectureInformation;
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
        validateUserAuthorization(frame.getUser().getId(), userId);
        TimetableLecture timetableLecture = request.toTimetableLecture(frame);
        List<TimetableCustomLectureInformation> timetableCustomLectureInformations = request.toTimetableCustomLectureInformations();
        for (TimetableCustomLectureInformation timetableCustomLectureInformation : timetableCustomLectureInformations) {
            timetableLecture.addTimetableCustomLectureInformation(timetableCustomLectureInformation);
        }
        timetableLectureRepositoryV3.save(timetableLecture);
        return getTimetableLectureResponse(userId, frame);
    }

    private TimetableLectureResponseV3 getTimetableLectureResponse(Integer userId, TimetableFrame timetableFrame) {
        int grades = calculateGradesMainFrame(timetableFrame);
        int totalGrades = calculateTotalGrades(timetableFrameRepositoryV3.findByUserIdAndIsMainTrue(userId));
        return TimetableLectureResponseV3.of(timetableFrame, grades, totalGrades);
    }

    @Transactional
    public TimetableLectureResponseV3 updateTimetablesCustomLecture(
        TimetableCustomLectureUpdateRequest request, Integer userId
    ) {
        TimetableFrame frame = timetableFrameRepositoryV3.getById(request.timetableFrameId());
        validateUserAuthorization(frame.getUser().getId(), userId);
        TimetableLecture timetableLecture = frame.getTimetableLectures().stream()
            .filter(l -> Objects.equals(l.getId(), request.timetableLecture().id()))
            .findFirst()
            .orElseThrow(() -> TimetableLectureNotFoundException.withDetail("id: " + request.timetableLecture().id()));

        timetableLecture.customLectureUpdate(
            request.timetableLecture().classTitle(),
            request.timetableLecture().professor()
        );

        timetableLecture.getTimetableCustomLectureInformations().clear();

        List<LectureInfo> lectureInfos = request.timetableLecture().lectureInfos();
        List<TimetableCustomLectureInformation> timetableCustomLectureInformations = lectureInfos.stream()
            .map(l -> TimetableCustomLectureInformation.builder()
                .timetableLecture(timetableLecture)
                .startTime(l.startTime())
                .endTime(l.endTime())
                .place(l.place())
                .build()
            )
            .toList();

        for (TimetableCustomLectureInformation timetableCustomLectureInformation : timetableCustomLectureInformations) {
            timetableLecture.addTimetableCustomLectureInformation(timetableCustomLectureInformation);
        }

        timetableLectureRepositoryV3.save(timetableLecture);
        return getTimetableLectureResponse(userId, frame);
    }
}
