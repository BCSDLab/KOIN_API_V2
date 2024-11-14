package in.koreatech.koin.domain.timetableV2.service;

import static in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureCreateRequest.InnerTimeTableLectureRequest;
import static in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureUpdateRequest.InnerTimetableLectureRequest;
import static in.koreatech.koin.domain.timetableV2.model.TimetableFrame.getDefaultTimetableFrameName;
import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateGrades;
import static in.koreatech.koin.domain.timetableV2.util.GradeCalculator.calculateTotalGrades;
import static in.koreatech.koin.domain.timetableV2.validation.TimetableFrameValidate.validateUserAuthorization;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV2.dto.request.TimetableFrameCreateRequest;
import in.koreatech.koin.domain.timetableV2.dto.request.TimetableFrameUpdateRequest;
import in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureCreateRequest;
import in.koreatech.koin.domain.timetableV2.dto.request.TimetableLectureUpdateRequest;
import in.koreatech.koin.domain.timetableV2.dto.response.TimetableFrameResponse;
import in.koreatech.koin.domain.timetableV2.dto.response.TimetableFrameUpdateResponse;
import in.koreatech.koin.domain.timetableV2.dto.response.TimetableLectureResponse;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.model.TimetableLecture;
import in.koreatech.koin.domain.timetableV2.repository.LectureRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.SemesterRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableLectureRepositoryV2;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import in.koreatech.koin.global.concurrent.ConcurrencyGuard;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableServiceV2 {

    private final LectureRepositoryV2 lectureRepositoryV2;
    private final TimetableLectureRepositoryV2 timetableLectureRepositoryV2;
    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;
    private final UserRepository userRepository;
    private final SemesterRepositoryV2 semesterRepositoryV2;

    @Transactional
    public TimetableFrameResponse createTimetablesFrame(Integer userId, TimetableFrameCreateRequest request) {
        Semester semester = semesterRepositoryV2.getBySemester(request.semester());
        User user = userRepository.getById(userId);
        int currentFrameCount = timetableFrameRepositoryV2.countByUserIdAndSemesterId(userId, semester.getId());
        boolean isMain = (currentFrameCount == 0);
        String name = (request.timetableName() != null) ? request.timetableName() :
            getDefaultTimetableFrameName(currentFrameCount + 1);
        TimetableFrame timetableFrame = request.toTimetablesFrame(user, semester, name, isMain);
        TimetableFrame savedTimetableFrame = timetableFrameRepositoryV2.save(timetableFrame);
        return TimetableFrameResponse.from(savedTimetableFrame);
    }

    @Transactional
    public TimetableFrameUpdateResponse updateTimetableFrame(
        TimetableFrameUpdateRequest request, Integer timetableFrameId, Integer userId
    ) {
        TimetableFrame timeTableFrame = timetableFrameRepositoryV2.getById(timetableFrameId);
        Semester semester = timeTableFrame.getSemester();
        boolean isMain = request.isMain();
        validateTimetableFrameUpdate(userId, isMain, semester, timeTableFrame);
        timeTableFrame.updateTimetableFrame(semester, request.timetableName(), isMain);
        return TimetableFrameUpdateResponse.from(timeTableFrame);
    }

    private void validateTimetableFrameUpdate(Integer userId, boolean isMain, Semester semester,
        TimetableFrame timeTableFrame) {
        if (isMain) {
            cancelMainTimetable(userId, semester.getId());
            return;
        }
        if (timeTableFrame.isMain()) {
            throw new KoinIllegalArgumentException("메인 시간표는 필수입니다.");
        }
    }

    private void cancelMainTimetable(Integer userId, Integer semesterId) {
        TimetableFrame mainTimetableFrame = timetableFrameRepositoryV2.getMainTimetableByUserIdAndSemesterId(userId,
            semesterId);
        mainTimetableFrame.cancelMain();
    }

    public List<TimetableFrameResponse> getTimetablesFrame(Integer userId, String semesterRequest) {
        Semester semester = semesterRepositoryV2.getBySemester(semesterRequest);
        return timetableFrameRepositoryV2.findAllByUserIdAndSemesterId(userId, semester.getId()).stream()
            .map(TimetableFrameResponse::from)
            .toList();
    }

    /*TODO. 락 3개 찾아보기 - 신관규*/
    @ConcurrencyGuard(lockName = "deleteFrame")
    public void deleteTimetablesFrame(Integer userId, Integer frameId) {
        TimetableFrame timetableFrame = timetableFrameRepositoryV2.getByIdWithLock(frameId);
        validateUserAuthorization(timetableFrame.getUser().getId(), userId);

        timetableFrameRepositoryV2.deleteById(frameId);
        if (timetableFrame.isMain()) {
            TimetableFrame nextFrame = timetableFrameRepositoryV2.findNextFirstTimetableFrame(userId,
                timetableFrame.getSemester().getId());
            if (nextFrame != null) {
                nextFrame.updateStatusMain(true);
            }
        }
    }

    @Transactional
    public TimetableLectureResponse createTimetableLectures(Integer userId, TimetableLectureCreateRequest request) {
        TimetableFrame timetableFrame = timetableFrameRepositoryV2.getById(request.timetableFrameId());
        validateUserAuthorization(timetableFrame.getUser().getId(), userId);

        for (InnerTimeTableLectureRequest timetableLectureRequest : request.timetableLecture()) {
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

        for (InnerTimetableLectureRequest timetableRequest : request.timetableLecture()) {
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
        List<TimetableLecture> timetableLectures = timetableFrame.getTimetableLectures();
        int grades = calculateGrades(timetableFrame, timetableLectures);
        int totalGrades = calculateTotalGrades(timetableFrameRepositoryV2.findByUserIdAndIsMainTrue(userId));
        return TimetableLectureResponse.of(timetableFrame.getId(), timetableLectures, grades, totalGrades);
    }

    @Transactional
    public void deleteAllTimetablesFrame(Integer userId, String semester) {
        User user = userRepository.getById(userId);
        Semester timetableSemester = semesterRepositoryV2.getBySemester(semester);
        timetableFrameRepositoryV2.deleteAllByUserAndSemester(user, timetableSemester);
    }

    @Transactional
    public void deleteTimetableLectures(List<Integer> request, Integer userId) {
        for (int timetablesLectureId : request) {
            TimetableLecture timetableLecture = timetableLectureRepositoryV2.getById(timetablesLectureId);
            if (!Objects.equals(timetableLecture.getTimetableFrame().getUser().getId(), userId)) {
                throw AuthorizationException.withDetail("userId: " + userId);
            }
            timetableLectureRepositoryV2.deleteById(timetablesLectureId);
        }
    }

    @Transactional
    public void deleteTimetableLectureByFrameId(Integer frameId, Integer lectureId, Integer userId) {
        TimetableFrame timetableFrame = timetableFrameRepositoryV2.getById(frameId);
        if (!Objects.equals(timetableFrame.getUser().getId(), userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }
        timetableLectureRepositoryV2.deleteByFrameIdAndLectureId(frameId, lectureId);
    }
}
