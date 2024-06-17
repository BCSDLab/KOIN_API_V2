package in.koreatech.koin.domain.timetable.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.dto.LectureResponse;
import in.koreatech.koin.domain.timetable.dto.TimetableCreateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableResponse;
import in.koreatech.koin.domain.timetable.dto.TimetableUpdateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableFrameUpdateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableFrameUpdateResponse;
import in.koreatech.koin.domain.timetable.exception.SemesterNotFoundException;
import in.koreatech.koin.domain.timetable.exception.TimetableLectureNotFoundException;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetable.model.Timetable;
import in.koreatech.koin.domain.timetable.model.TimetableFrame;
import in.koreatech.koin.domain.timetable.model.TimetableLecture;
import in.koreatech.koin.domain.timetable.repository.LectureRepository;
import in.koreatech.koin.domain.timetable.repository.SemesterRepository;
import in.koreatech.koin.domain.timetable.repository.TimetableFrameRepository;
import in.koreatech.koin.domain.timetable.repository.TimetableLectureRepository;
import in.koreatech.koin.domain.timetable.repository.TimetableRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import in.koreatech.koin.global.auth.exception.AuthorizationException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableService {

    private final LectureRepository lectureRepository;
    private final SemesterRepository semesterRepository;
    private final TimetableRepository timetableRepository;
    private final TimetableLectureRepository timetableLectureRepository;
    private final TimetableFrameRepository timetableFrameRepository;
    private final UserRepository userRepository;

    public List<LectureResponse> getLecturesBySemester(String semester) {
        List<Lecture> lectures = lectureRepository.findBySemester(semester);
        if (lectures.isEmpty()) {
            throw SemesterNotFoundException.withDetail(semester);
        }
        return lectures.stream()
            .map(LectureResponse::from)
            .toList();
    }

    public TimetableResponse getTimetables(Integer userId, String semesterRequest) {
        Semester semester = semesterRepository.getBySemester(semesterRequest);
        return getTimetableResponse(userId, semester);
    }

    @Transactional
    public TimetableResponse createTimetables(Integer userId, TimetableCreateRequest request) {
        User user = userRepository.getById(userId);
        Semester semester = semesterRepository.getBySemester(request.semester());
        for (TimetableCreateRequest.InnerTimetableRequest timetableRequest : request.timetable()) {
            Timetable timeTable = timetableRequest.toTimetable(user, semester);
            timetableRepository.save(timeTable);
        }
        return getTimetableResponse(userId, semester);
    }

    @Transactional
    public TimetableResponse updateTimetables(Integer userId, TimetableUpdateRequest request) {
        Semester semester = semesterRepository.getBySemester(request.semester());
        for (TimetableUpdateRequest.InnerTimetableRequest timetableRequest : request.timetable()) {
            Timetable timeTable = timetableRepository.getById(timetableRequest.id());
            timeTable.update(timetableRequest);
        }
        return getTimetableResponse(userId, semester);
    }

    private TimetableResponse getTimetableResponse(Integer userId, Semester semester) {
        TimetableFrame mainTimetableFrame = findMainTimetable(userId, semester.getId());
        List<TimetableLecture> timetableLecture = timetableLectureRepository.findAllByTimetableFrameId(mainTimetableFrame.getId());

        List<Integer> lectureIds = timetableLecture.stream().map(tl -> tl.getLecture().getId()).toList();
        List<Lecture> lectures = lectureRepository.findAllByIds(lectureIds);

        Integer grades = lectures.stream()
            .mapToInt(lecture -> Integer.parseInt(lecture.getGrades()))
            .sum();
        Integer totalGrades = calculateTotalGrades(userId);

        return TimetableResponse.of(semester.getSemester(), timetableLecture, lectures, grades, totalGrades);
    }

    private int calculateTotalGrades(Integer userId) {
        int totalGrades = 0;
        List<Semester> semesters = semesterRepository.findAllByOrderBySemesterDesc();

        for (Semester semester : semesters) {
            totalGrades += timetableRepository.findAllByUserIdAndSemesterId(userId, semester.getId()).stream()
                .mapToInt(timeTable -> Integer.parseInt(timeTable.getGrades()))
                .sum();
        }

        return totalGrades;
    }

    @Transactional
    public void deleteTimetableLecture(Integer userId, Integer timetableLectureId) {
        timetableLectureRepository.getById(timetableLectureId);
        TimetableFrame frame = timetableFrameRepository.getById(timetableLectureId);
        if (!Objects.equals(frame.getUser().getId(), userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }

        timetableLectureRepository.deleteById(timetableLectureId);
    }

    @Transactional
    public TimetableFrameUpdateResponse updateTimetableFrame(Integer timetableFrameId,
        TimetableFrameUpdateRequest timetableFrameUpdateRequest, Integer userId) {
        TimetableFrame timeTableFrame = timetableFrameRepository.getById(timetableFrameId);
        Semester semester = semesterRepository.getBySemester(timetableFrameUpdateRequest.semester());
        boolean isMain = timetableFrameUpdateRequest.isMain();
        if (isMain) {
            cancelMainTimetable(userId, semester.getId());
        }
        timeTableFrame.updateTimetableFrame(semester, timetableFrameUpdateRequest.name(),
            timetableFrameUpdateRequest.isMain());
        return TimetableFrameUpdateResponse.from(timeTableFrame);
    }

    private void cancelMainTimetable(Integer userId, Integer semesterId) {
        TimetableFrame mainTimetableFrame = findMainTimetable(userId, semesterId);
        mainTimetableFrame.cancelMain();
    }

    private TimetableFrame findMainTimetable(Integer userId, Integer semesterId) {
        return timetableFrameRepository.findAllByUserIdAndSemesterId(userId, semesterId)
            .stream()
            .filter(TimetableFrame::isMain)
            .findFirst()
            .orElseThrow(() -> TimetableLectureNotFoundException.withDetail("못 찾음 userId: "+ userId + ", semesterId: " + semesterId));
    }
}
