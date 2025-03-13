package in.koreatech.koin.domain.timetableV2.service;

import static in.koreatech.koin.domain.timetableV2.model.TimetableFrame.*;
import static in.koreatech.koin.domain.timetableV2.validation.TimetableFrameValidate.validateMainTimetableRequired;
import static in.koreatech.koin.domain.timetableV2.validation.TimetableFrameValidate.validateUserOwnsFrame;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV2.dto.request.TimetableFrameCreateRequest;
import in.koreatech.koin.domain.timetableV2.dto.request.TimetableFrameUpdateRequest;
import in.koreatech.koin.domain.timetableV2.dto.response.TimetableFrameResponse;
import in.koreatech.koin.domain.timetableV2.dto.response.TimetableFramesResponse;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV2.repository.SemesterRepositoryV2;
import in.koreatech.koin.domain.timetableV2.repository.TimetableFrameRepositoryV2;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableFrameService {

    private final TimetableFrameRepositoryV2 timetableFrameRepositoryV2;
    private final UserRepository userRepository;
    private final SemesterRepositoryV2 semesterRepositoryV2;

    @Transactional
    public TimetableFrameResponse createTimetablesFrame(Integer userId, TimetableFrameCreateRequest request) {
        Semester semester = semesterRepositoryV2.getBySemester(request.semester());
        User user = userRepository.getById(userId);
        int currentFrameCount = timetableFrameRepositoryV2.countByUserIdAndSemesterId(userId, semester.getId());

        boolean isMain = isMainFrame(currentFrameCount);
        String name = getTimetableName(request.timetableName(), currentFrameCount);
        TimetableFrame frame = request.toTimetablesFrame(user, semester, name, isMain);

        return TimetableFrameResponse.from(timetableFrameRepositoryV2.save(frame));
    }

    @Transactional
    public TimetableFrameResponse updateTimetableFrame(
        TimetableFrameUpdateRequest request, Integer timetableFrameId, Integer userId
    ) {
        TimetableFrame frame = timetableFrameRepositoryV2.getById(timetableFrameId);
        validateMainTimetableRequired(frame, request.isMain());

        if (request.isMain()) {
            timetableFrameRepositoryV2
                .findByUserIdAndSemesterIdAndIsMainTrue(userId, frame.getSemester().getId())
                .ifPresent(TimetableFrame::cancelMain);
        }
        frame.renameAndSetMain(request.timetableName(), request.isMain());

        return TimetableFrameResponse.from(timetableFrameRepositoryV2.save(frame));
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

    public TimetableFramesResponse getAllTimetablesFrame(Integer userId) {
        List<TimetableFrame> timetableFrames = timetableFrameRepositoryV2.getAllByUserId(userId);
        return TimetableFramesResponse.from(timetableFrames);
    }

    @Transactional
    public void deleteAllTimetablesFrame(Integer userId, String semester) {
        User user = userRepository.getById(userId);
        Semester timetableSemester = semesterRepositoryV2.getBySemester(semester);
        timetableFrameRepositoryV2.findAllByUserAndSemester(user, timetableSemester)
            .forEach(TimetableFrame::delete);
    }

    @Transactional
    public void deleteTimetablesFrame(Integer userId, Integer frameId) {
        TimetableFrame timetableFrame = timetableFrameRepositoryV2.getByIdWithLock(frameId);
        validateUserOwnsFrame(timetableFrame.getUser().getId(), userId);

        deleteFrameAndUpdateMainStatus(userId, timetableFrame);
    }

    private void deleteFrameAndUpdateMainStatus(Integer userId, TimetableFrame frame) {
        frame.delete();
        if (frame.isMain()) {
            frame.setMain(false);
            TimetableFrame nextFrame = timetableFrameRepositoryV2.findNextFirstTimetableFrame(userId,
                frame.getSemester().getId());
            if (nextFrame != null) {
                nextFrame.setMain(true);
            }
        }
    }
}
