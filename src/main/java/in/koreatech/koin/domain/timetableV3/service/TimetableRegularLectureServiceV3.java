package in.koreatech.koin.domain.timetableV3.service;

import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateGradesMainFrame;
import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateTotalGrades;
import static in.koreatech.koin.domain.timetableV2.validation.TimetableFrameValidate.validateUserAuthorization;
import static in.koreatech.koin.domain.timetableV3.dto.request.TimetableRegularLectureUpdateRequest.InnerTimeTableRegularLectureRequest.ClassPlace;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV3.dto.request.TimetableRegularLectureCreateRequest;
import in.koreatech.koin.domain.timetableV3.dto.request.TimetableRegularLectureUpdateRequest;
import in.koreatech.koin.domain.timetableV3.dto.response.TimetableLectureResponseV3;
import in.koreatech.koin.domain.timetableV3.model.LectureInformation;
import in.koreatech.koin.domain.timetableV3.model.TimetableRegularLectureInformation;
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

    @Transactional
    public TimetableLectureResponseV3 createTimetablesRegularLecture(
        TimetableRegularLectureCreateRequest request, Integer userId
    ) {
        TimetableFrame frame = timetableFrameRepositoryV3.getById(request.timetableFrameId());
        validateUserAuthorization(frame.getUser().getId(), userId);
        Lecture lecture = lectureRepositoryV3.getById(request.lectureId());
        TimetableLecture timetableLecture = request.toTimetableLecture(frame, lecture);
        timetableLectureRepositoryV3.save(timetableLecture);
        return getTimetableLectureResponse(userId, frame);
    }

    private TimetableLectureResponseV3 getTimetableLectureResponse(Integer userId, TimetableFrame timetableFrame) {
        int grades = calculateGradesMainFrame(timetableFrame);
        int totalGrades = calculateTotalGrades(timetableFrameRepositoryV3.findByUserIdAndIsMainTrue(userId));
        return TimetableLectureResponseV3.of(timetableFrame, grades, totalGrades);
    }

    @Transactional
    public TimetableLectureResponseV3 updateTimetablesRegularLecture(
        TimetableRegularLectureUpdateRequest request, Integer userId
    ) {
        TimetableFrame frame = timetableFrameRepositoryV3.getById(request.timetableFrameId());
        validateUserAuthorization(frame.getUser().getId(), userId);
        // TODO. 강의 장소 개수와 강의 시간 개수가 일치 하지 않으면 예외 던지기
        TimetableLecture timetableLecture = timetableLectureRepositoryV3.getById(request.timetableLecture().id());
        if (!timetableLecture.getLecture().getName().equals(request.timetableLecture().classTitle())) {
            timetableLecture.regularLectureUpdate(request.timetableLecture().classTitle());
        }

        timetableLecture.getTimetableRegularLectureInformations().clear();
        List<LectureInformation> lectureInformations = timetableLecture.getLecture().getLectureInformations();
        List<ClassPlace> classPlaces = request.timetableLecture().classPlaces();

        for (int index = 0; index < lectureInformations.size(); index++) {
            TimetableRegularLectureInformation timetableRegularLectureInformation = TimetableRegularLectureInformation.builder()
                .timetableLecture(timetableLecture)
                .lectureInformation(lectureInformations.get(index))
                .place(classPlaces.get(index).classPlace())
                .build();

            timetableRegularLectureInformation.setLectureInformationId(lectureInformations.get(index));
            timetableRegularLectureInformation.setTimetableLectureId(timetableLecture);
        }

        timetableLectureRepositoryV3.save(timetableLecture);
        return getTimetableLectureResponse(userId, frame);
    }
}
