package in.koreatech.koin.domain.timetableV2.service;

import static in.koreatech.koin.domain.timetableV2.dto.TimetableLectureCreateRequest.*;
import static in.koreatech.koin.domain.timetableV2.dto.TimetableLectureUpdateRequest.*;

import java.util.List;
import java.util.Objects;

import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV2.dto.TimetableFrameCreateRequest;
import in.koreatech.koin.domain.timetableV2.dto.TimetableFrameResponse;
import in.koreatech.koin.domain.timetableV2.dto.TimetableFrameUpdateRequest;
import in.koreatech.koin.domain.timetableV2.dto.TimetableFrameUpdateResponse;
import in.koreatech.koin.domain.timetableV2.dto.TimetableLectureCreateRequest;
import in.koreatech.koin.domain.timetableV2.dto.TimetableLectureResponse;
import in.koreatech.koin.domain.timetableV2.dto.TimetableLectureUpdateRequest;
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

        TimetableFrame timetableFrame = request.toTimetablesFrame(user, semester, "시간표" + (currentFrameCount+1), isMain);
        TimetableFrame savedTimetableFrame = timetableFrameRepositoryV2.save(timetableFrame);
        return TimetableFrameResponse.from(savedTimetableFrame);
    }

    @Transactional
    public TimetableFrameUpdateResponse updateTimetableFrame(Integer timetableFrameId,
        TimetableFrameUpdateRequest timetableFrameUpdateRequest, Integer userId) {
        TimetableFrame timeTableFrame = timetableFrameRepositoryV2.getById(timetableFrameId);
        Semester semester = timeTableFrame.getSemester();
        boolean isMain = timetableFrameUpdateRequest.isMain();
        if (isMain) {
            cancelMainTimetable(userId, semester.getId());
        } else {
            if (timeTableFrame.isMain()) {
                throw new KoinIllegalArgumentException("메인 시간표는 필수입니다.");
            }
        }
        timeTableFrame.updateTimetableFrame(semester, timetableFrameUpdateRequest.name(), isMain);
        return TimetableFrameUpdateResponse.from(timeTableFrame);
    }

    public List<TimetableFrameResponse> getTimetablesFrame(Integer userId, String semesterRequest) {
        Semester semester = semesterRepositoryV2.getBySemester(semesterRequest);
        return timetableFrameRepositoryV2.findAllByUserIdAndSemesterId(userId, semester.getId()).stream()
            .map(TimetableFrameResponse::from)
            .toList();
    }

    // @ConcurrencyGuard(lockName = "deleteFrame")
    @Transactional
    public void deleteTimetablesFrame(Integer userId, Integer frameId) {
        TimetableFrame frame = timetableFrameRepositoryV2.getByIdWithLock(frameId);
        if (!Objects.equals(frame.getUser().getId(), userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }
        timetableFrameRepositoryV2.deleteById(frameId);
        if (frame.isMain()) {
            TimetableFrame nextMainFrame =
                timetableFrameRepositoryV2.
                    findFirstByUserIdAndSemesterIdAndIsMainFalseOrderByCreatedAtAsc(userId, frame.getSemester().getId());
            if (nextMainFrame != null) {
                nextMainFrame.updateStatusMain(true);
            }
        }
    }

    @Transactional
    public TimetableLectureResponse createTimetableLectures(Integer userId, TimetableLectureCreateRequest request) {
        TimetableFrame timetableFrame = timetableFrameRepositoryV2.getById(request.timetableFrameId());
        if (!Objects.equals(timetableFrame.getUser().getId(), userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }

        for (InnerTimeTableLectureRequest timetableLectureRequest : request.timetableLecture()) {
            Lecture lecture = timetableLectureRequest.lectureId() == null ?
                null : lectureRepositoryV2.getLectureById(timetableLectureRequest.lectureId());
            TimetableLecture timetableLecture = timetableLectureRequest.toTimetableLecture(timetableFrame, lecture);
            timetableLectureRepositoryV2.save(timetableLecture);
        }

        List<TimetableLecture> timetableLectures = timetableLectureRepositoryV2.findAllByTimetableFrameId(
            timetableFrame.getId());
        return getTimetableLectureResponse(userId, timetableFrame, timetableLectures);
    }

    @Transactional
    public TimetableLectureResponse updateTimetablesLectures(Integer userId, TimetableLectureUpdateRequest request) {
        TimetableFrame timetableFrame = timetableFrameRepositoryV2.getById(request.timetableFrameId());
        if (!Objects.equals(timetableFrame.getUser().getId(), userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }

        for (InnerTimetableLectureRequest timetableRequest : request.timetableLecture()) {
            TimetableLecture timetableLecture = timetableLectureRepositoryV2.getById(timetableRequest.id());
            if (timetableRequest.lectureId() == null) {
                timetableLecture.update(
                    timetableRequest.classTitle(),
                    timetableRequest.classTime().toString(),
                    timetableRequest.classPlace(),
                    timetableRequest.professor(),
                    timetableRequest.grades(),
                    timetableRequest.memo());
            }
        }
        List<TimetableLecture> timetableLectures = timetableFrame.getTimetableLectures();
        return getTimetableLectureResponse(userId, timetableFrame, timetableLectures);
    }

    @Transactional
    public TimetableLectureResponse getTimetableLectures(Integer userId, Integer timetableFrameId) {
        TimetableFrame frame = timetableFrameRepositoryV2.getById(timetableFrameId);
        List<TimetableLecture> timetableLectures = frame.getTimetableLectures();
        if (!Objects.equals(frame.getUser().getId(), userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }
        return getTimetableLectureResponse(userId, frame, timetableLectures);
    }

    @Transactional
    public void deleteTimetableLecture(Integer userId, Integer timetableLectureId) {
        TimetableLecture timetableLecture = timetableLectureRepositoryV2.getById(timetableLectureId);
        TimetableFrame frame = timetableLecture.getTimetableFrame();
        if (!Objects.equals(frame.getUser().getId(), userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }
        timetableLectureRepositoryV2.deleteById(timetableLectureId);
    }

    private TimetableLectureResponse getTimetableLectureResponse(Integer userId, TimetableFrame timetableFrame,
        List<TimetableLecture> timetableLectures) {
        int grades = 0;
        int totalGrades = 0;

        if (timetableFrame.isMain()) {
            grades = calculateGrades(timetableLectures);
        }

        for (TimetableFrame timetableFrames : timetableFrameRepositoryV2.findByUserIdAndIsMainTrue(userId)) {
            totalGrades += calculateGrades(
                timetableLectureRepositoryV2.findAllByTimetableFrameId(timetableFrames.getId()));
        }

        return TimetableLectureResponse.of(timetableFrame.getId(), timetableLectures, grades, totalGrades);
    }

    private int calculateGrades(List<TimetableLecture> timetableLectures) {
        return timetableLectures.stream()
            .mapToInt(lecture -> {
                if (lecture.getLecture() != null) {
                    return Integer.parseInt(lecture.getLecture().getGrades());
                } else {
                    return Integer.parseInt(lecture.getGrades());
                }
            })
            .sum();
    }

    private void cancelMainTimetable(Integer userId, Integer semesterId) {
        TimetableFrame mainTimetableFrame = timetableFrameRepositoryV2.getMainTimetableByUserIdAndSemesterId(userId,
            semesterId);
        mainTimetableFrame.cancelMain();
    }
}
