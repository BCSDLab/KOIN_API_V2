package in.koreatech.koin.domain.timetableV2.service;

import static in.koreatech.koin.domain.timetableV2.model.TimetableFrame.getDefaultTimetableFrameName;
import static in.koreatech.koin.domain.timetableV2.validation.TimetableFrameValidate.validateUserAuthorization;

import java.util.List;

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
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.concurrent.ConcurrencyGuard;
import in.koreatech.koin.global.exception.KoinIllegalArgumentException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableFrameServiceV2 {
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
}
