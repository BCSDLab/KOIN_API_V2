package in.koreatech.koin.domain.timetableV3.service;

import static in.koreatech.koin.domain.timetableV2.validation.TimetableFrameValidate.validateMainTimetableRequired;
import static in.koreatech.koin.domain.timetableV3.model.Term.fromDescription;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetableV2.model.TimetableFrame;
import in.koreatech.koin.domain.timetableV3.dto.request.TimetableFrameCreateRequestV3;
import in.koreatech.koin.domain.timetableV3.dto.request.TimetableFrameUpdateRequestV3;
import in.koreatech.koin.domain.timetableV3.dto.response.TimetableFrameResponseV3;
import in.koreatech.koin.domain.timetableV3.dto.response.TimetableFramesResponseV3;
import in.koreatech.koin.domain.timetableV3.model.Term;
import in.koreatech.koin.domain.timetableV3.repository.SemesterRepositoryV3;
import in.koreatech.koin.domain.timetableV3.repository.TimetableFrameRepositoryV3;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableFrameServiceV3 {

    private static final String DEFAULT_TIMETABLE_FRAME_NAME = "시간표";

    private final TimetableFrameRepositoryV3 timetableFrameRepositoryV3;
    private final SemesterRepositoryV3 semesterRepositoryV3;
    private final UserRepository userRepository;

    @Transactional
    public List<TimetableFrameResponseV3> createTimetablesFrame(TimetableFrameCreateRequestV3 request, Integer userId) {
        Semester semester = semesterRepositoryV3.getByYearAndTerm(request.year(), fromDescription(request.term()));
        User user = userRepository.getById(userId);
        int currentFrameCount = timetableFrameRepositoryV3.countByUserIdAndSemesterId(userId, semester.getId());

        TimetableFrame frame = request.toTimetablesFrame(user, semester,
            getDefaultTimetableFrameName(currentFrameCount), determineIfMain(currentFrameCount));
        timetableFrameRepositoryV3.save(frame);

        List<TimetableFrame> frames = timetableFrameRepositoryV3.findByUserAndSemester(user, semester);
        frames.sort(Comparator.comparing(TimetableFrame::isMain).reversed().thenComparing(TimetableFrame::getId));
        return frames.stream()
            .map(TimetableFrameResponseV3::from)
            .toList();
    }

    private boolean determineIfMain(int currentFrameCount) {
        return currentFrameCount == 0;
    }

    private String getDefaultTimetableFrameName(int currentFrameCount) {
        return DEFAULT_TIMETABLE_FRAME_NAME + (currentFrameCount + 1);
    }

    @Transactional
    public List<TimetableFrameResponseV3> updateTimetableFrame(
        TimetableFrameUpdateRequestV3 request, Integer timetableFrameId, Integer userId
    ) {
        TimetableFrame frame = timetableFrameRepositoryV3.getById(timetableFrameId);
        validateMainTimetableRequired(frame, request.isMain());
        cancelIfMainTimetable(userId, frame.getSemester().getId(), request.isMain());
        frame.renameAndSetMain(request.name(), request.isMain());

        List<TimetableFrame> frames = timetableFrameRepositoryV3.findByUserAndSemester(frame.getUser(),
            frame.getSemester());
        frames.sort(Comparator.comparing(TimetableFrame::isMain).reversed().thenComparing(TimetableFrame::getId));
        return frames.stream()
            .map(TimetableFrameResponseV3::from)
            .toList();
    }

    private void cancelIfMainTimetable(Integer userId, Integer semesterId, boolean isMain) {
        if (isMain) {
            TimetableFrame mainTimetableFrame = timetableFrameRepositoryV3.getMainTimetableByUserIdAndSemesterId(userId,
                semesterId);
            mainTimetableFrame.setMain(false);
        }
    }

    public List<TimetableFrameResponseV3> getTimetablesFrame(Integer year, String term, Integer userId) {
        Semester semester = semesterRepositoryV3.getByYearAndTerm(year, Term.fromDescription(term));
        List<TimetableFrame> frames = timetableFrameRepositoryV3.findAllByUserIdAndSemesterId(userId, semester.getId());
        frames.sort(Comparator.comparing(TimetableFrame::isMain).reversed().thenComparing(TimetableFrame::getId));
        return frames.stream()
            .map(TimetableFrameResponseV3::from)
            .toList();
    }

    public List<TimetableFramesResponseV3> getTimetablesFrames(Integer userId) {
        List<TimetableFrame> timetableFrames = timetableFrameRepositoryV3.findAllByUserId(userId);
        return TimetableFramesResponseV3.from(timetableFrames);
    }

    @Transactional
    public void deleteTimetablesFrames(Integer year, String term, Integer userId) {
        User user = userRepository.getById(userId);
        Semester timetableSemester = semesterRepositoryV3.getByYearAndTerm(year, Term.fromDescription(term));
        timetableFrameRepositoryV3.findAllByUserAndSemester(user, timetableSemester)
            .forEach(TimetableFrame::delete);
    }
}
