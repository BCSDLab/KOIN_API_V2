package in.koreatech.koin.domain.timetableV2.service;

import static in.koreatech.koin.domain.timetableV2.validation.TimetableFrameValidate.validateTimetableFrameUpdate;
import static in.koreatech.koin.domain.timetableV2.validation.TimetableFrameValidate.validateUserAuthorization;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV2.dto.request.TimetableFrameCreateRequest;
import in.koreatech.koin.domain.timetableV2.dto.request.TimetableFrameUpdateRequest;
import in.koreatech.koin.domain.timetableV2.dto.response.TimetableFrameResponse;
import in.koreatech.koin.domain.timetableV2.dto.response.TimetableFrameUpdateResponse;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.repository.SemesterRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.timetableV2.factory.TimetableFrameCreator;
import in.koreatech.koin.domain.timetableV2.factory.TimetableFrameUpdater;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.concurrent.ConcurrencyGuard;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableFrameService {

    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;
    private final UserRepository userRepository;
    private final SemesterRepositoryV2 semesterRepositoryV2;
    private final TimetableFrameCreator timetableFrameCreator;
    private final TimetableFrameUpdater timetableFrameUpdater;

    @Transactional
    public TimetableFrameResponse createTimetablesFrame(Integer userId, TimetableFrameCreateRequest request) {
        Semester semester = semesterRepositoryV2.getBySemester(request.semester());
        User user = userRepository.getById(userId);
        int currentFrameCount = timetableFrameRepositoryV2.countByUserIdAndSemesterId(userId, semester.getId());

        TimetableFrame frame = timetableFrameCreator.createTimetableFrame(request, user, semester, currentFrameCount);
        TimetableFrame saveFrame = timetableFrameRepositoryV2.save(frame);

        return TimetableFrameResponse.from(saveFrame);
    }

    @Transactional
    public TimetableFrameUpdateResponse updateTimetableFrame(
        TimetableFrameUpdateRequest request, Integer timetableFrameId, Integer userId
    ) {
        TimetableFrame frame = timetableFrameRepositoryV2.getById(timetableFrameId);
        validateTimetableFrameUpdate(frame, request.isMain());
        return timetableFrameUpdater.updateTimetableFrame(frame, userId, request.timetableName(), request.isMain());
    }

    public Object getTimetablesFrame(Integer userId, String semesterRequest) {
        if (semesterRequest == null) {
            return getAllTimetablesFrame(userId);
        }

        Semester semester = semesterRepositoryV2.getBySemester(semesterRequest);
        return timetableFrameRepositoryV2.findAllByUserIdAndSemesterId(userId, semester.getId()).stream()
            .map(TimetableFrameResponse::from)
            .toList();
    }

    public Map<String, Map<String, List<TimetableFrameResponse>>> getAllTimetablesFrame(Integer userId) {
        List<TimetableFrame> timetableFrames = timetableFrameRepositoryV2.findAllByUserId(userId);

        Map<String, List<TimetableFrameResponse>> groupedBySemester = timetableFrames.stream()
            .collect(Collectors.groupingBy(
                frame -> frame.getSemester().getSemester(),
                Collectors.mapping(TimetableFrameResponse::from, Collectors.toList())
            ));

        return Map.of("semesters", groupedBySemester);
    }

    @Transactional
    public void deleteAllTimetablesFrame(Integer userId, String semester) {
        User user = userRepository.getById(userId);
        Semester timetableSemester = semesterRepositoryV2.getBySemester(semester);
        timetableFrameRepositoryV2.deleteAllByUserAndSemester(user, timetableSemester);
    }

    @Transactional
    public void deleteTimetablesFrame(Integer userId, Integer frameId) {
        TimetableFrame timetableFrame = timetableFrameRepositoryV2.getByIdWithLock(frameId);
        validateUserAuthorization(timetableFrame.getUser().getId(), userId);

        deleteFrameAndUpdateMainStatusWithLock(frameId, userId, timetableFrame);
    }

    @ConcurrencyGuard(lockName = "deleteFrame")
    private void deleteFrameAndUpdateMainStatusWithLock(Integer frameId, Integer userId, TimetableFrame frame) {
        timetableFrameRepositoryV2.deleteById(frameId);
        if (frame.isMain()) {
            TimetableFrame nextFrame = timetableFrameRepositoryV2.findNextFirstTimetableFrame(userId,
                frame.getSemester().getId());
            if (nextFrame != null) {
                nextFrame.updateMainFlag(true);
            }
        }
    }
}
