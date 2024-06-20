package in.koreatech.koin.domain.timetableV2.service;

import java.util.List;
import java.util.Objects;

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
import in.koreatech.koin.domain.timetableV2.repository.LectureRepository;
import in.koreatech.koin.domain.timetableV2.repository.SemesterRepository;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepository;
import in.koreatech.koin.domain.timetableV2.repository.TimetableLectureRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableService {

    private final LectureRepository lectureRepository;
    private final TimetableLectureRepository timetableLectureRepository;
    private final TimetableFrameRepository timetableFrameRepository;
    private final UserRepository userRepository;
    private final SemesterRepository semesterRepository;

    @Transactional
    public TimetableFrameResponse createTimetablesFrame(Integer userId, TimetableFrameCreateRequest request) {
        Semester semester = semesterRepository.getBySemester(request.semester());
        User user = userRepository.getById(userId);
        int currentFrameCount = timetableFrameRepository.countByUserIdAndSemesterId(userId, semester.getId()) + 1;
        boolean isMain = (currentFrameCount == 1);

        TimetableFrame timeTableFrame = request.toTimetablesFrame(user, semester, "시간표" + currentFrameCount, isMain);
        return TimetableFrameResponse.from(timetableFrameRepository.save(timeTableFrame));
    }

    @Transactional
    public TimetableFrameUpdateResponse updateTimetableFrame(Integer timetableFrameId,
        TimetableFrameUpdateRequest timetableFrameUpdateRequest, Integer userId) {
        TimetableFrame timeTableFrame = timetableFrameRepository.getById(timetableFrameId);
        Semester semester = timeTableFrame.getSemester();
        boolean isMain = timetableFrameUpdateRequest.isMain();
        if (isMain) {
            cancelMainTimetable(userId, semester.getId());
        }
        timeTableFrame.updateTimetableFrame(semester, timetableFrameUpdateRequest.name(), isMain);
        return TimetableFrameUpdateResponse.from(timeTableFrame);
    }

    public List<TimetableFrameResponse> getTimetablesFrame(Integer userId, String semesterRequest) {
        Semester semester = semesterRepository.getBySemester(semesterRequest);
        return timetableFrameRepository.findAllByUserIdAndSemesterId(userId, semester.getId()).stream()
            .map(TimetableFrameResponse::from)
            .toList();
    }

    @Transactional
    public void deleteTimetablesFrame(Integer userId, Integer frameId) {
        TimetableFrame frame = timetableFrameRepository.getById(frameId);
        if (!Objects.equals(frame.getUser().getId(), userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }
        if (frame.getIsMain()) {
            TimetableFrame nextMainFrame =
                timetableFrameRepository.
                    findFirstByUserIdAndSemesterIdAndIsMainFalseOrderByCreatedAtAsc(userId,
                        frame.getSemester().getId());
            if (nextMainFrame != null) {
                nextMainFrame.updateStatusMain(true);
                timetableFrameRepository.save(nextMainFrame);
            }
        }
        timetableFrameRepository.deleteById(frameId);
    }

    @Transactional
    public TimetableLectureResponse createTimetableLectures(Integer userId, TimetableLectureCreateRequest request) {
        TimetableFrame timetableFrame = timetableFrameRepository.getById(request.timetableFrameId());
        if (!Objects.equals(timetableFrame.getUser().getId(), userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }

        for (TimetableLectureCreateRequest.InnerTimeTableLectureRequest timetableLectureRequest : request.timetableLecture()) {
            if (timetableLectureRequest.lectureId() == null) {
                TimetableLecture timetableLecture = timetableLectureRequest.toTimetableLecture(timetableFrame);
                timetableLectureRepository.save(timetableLecture);
            } else {
                Lecture lecture = lectureRepository.getLectureById(timetableLectureRequest.lectureId());
                TimetableLecture timetableLecture = timetableLectureRequest.toTimetableLecture(timetableFrame, lecture);
                timetableLectureRepository.save(timetableLecture);
            }
        }

        List<TimetableLecture> timetableLectures = timetableLectureRepository.findAllByTimetableFrameId(
            timetableFrame.getId());
        return getTimetableLectureResponse(userId, timetableFrame, timetableLectures);
    }

    @Transactional
    public TimetableLectureResponse updateTimetablesLectures(Integer userId, TimetableLectureUpdateRequest request) {
        TimetableFrame timetableFrame = timetableFrameRepository.getById(request.timetableFrameId());
        if (!Objects.equals(timetableFrame.getUser().getId(), userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }

        for (TimetableLectureUpdateRequest.InnerTimetableLectureRequest timetableRequest : request.timetableLecture()) {
            TimetableLecture timetableLecture = timetableLectureRepository.getById(timetableRequest.id());
            if (timetableRequest.lectureId() == null) {
                timetableLecture.update(timetableRequest);
                timetableLectureRepository.save(timetableLecture);
            }
        }
        List<TimetableLecture> timetableLectures = timetableLectureRepository.findAllByTimetableFrameId(
            timetableFrame.getId());
        return getTimetableLectureResponse(userId, timetableFrame, timetableLectures);
    }

    @Transactional
    public TimetableLectureResponse getTimetableLectures(Integer userId, Integer timetableFrameId) {
        List<TimetableLecture> timetableLectures = timetableLectureRepository.findAllByTimetableFrameId(
            timetableFrameId);
        TimetableFrame frame = timetableFrameRepository.getById(timetableFrameId);
        if (!Objects.equals(frame.getUser().getId(), userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }
        return getTimetableLectureResponse(userId, frame, timetableLectures);
    }

    @Transactional
    public void deleteTimetableLecture(Integer userId, Integer timetableLectureId) {
        TimetableLecture timetableLecture = timetableLectureRepository.getById(timetableLectureId);
        TimetableFrame frame = timetableFrameRepository.getById(timetableLecture.getTimetableFrame().getId());
        if (!Objects.equals(frame.getUser().getId(), userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }

        timetableLectureRepository.deleteById(timetableLectureId);
    }

    private TimetableLectureResponse getTimetableLectureResponse(Integer userId, TimetableFrame timetableFrame,
        List<TimetableLecture> timetableLectures) {
        int grades = 0;
        int totalGrades = 0;

        if (timetableFrame.getIsMain()) {
            grades = calculateGrades(timetableLectures);
        }

        for (TimetableFrame timetableFrames : timetableFrameRepository.findByUserIdAndIsMainTrue(userId)) {
            totalGrades += calculateGrades(
                timetableLectureRepository.findAllByTimetableFrameId(timetableFrames.getId()));
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
        TimetableFrame mainTimetableFrame = timetableFrameRepository.getMainTimetableByUserIdAndSemesterId(userId,
            semesterId);
        mainTimetableFrame.cancelMain();
    }
}
