package in.koreatech.koin.domain.timetable.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.dto.LectureResponse;
import in.koreatech.koin.domain.timetable.dto.TimeTableCreateRequest;
import in.koreatech.koin.domain.timetable.dto.TimeTableResponse;
import in.koreatech.koin.domain.timetable.dto.TimeTableUpdateRequest;
import in.koreatech.koin.domain.timetable.dto.TimeTableFrameRequest;
import in.koreatech.koin.domain.timetable.dto.TimeTableFrameResponse;
import in.koreatech.koin.domain.timetable.exception.SemesterNotFoundException;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetable.model.TimeTable;
import in.koreatech.koin.domain.timetable.model.TimeTableFrame;
import in.koreatech.koin.domain.timetable.repository.LectureRepository;
import in.koreatech.koin.domain.timetable.repository.SemesterRepository;
import in.koreatech.koin.domain.timetable.repository.TimeTableFrameRepository;
import in.koreatech.koin.domain.timetable.repository.TimeTableRepository;
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
    private final TimeTableRepository timeTableRepository;
    private final TimeTableFrameRepository timetableFrameRepository;
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

    @Transactional
    public TimeTableFrameResponse createTimetablesFrame(Integer userId, TimeTableFrameRequest request) {
        Semester semester = semesterRepository.getBySemester(request.semester());
        User user = userRepository.getById(userId);
        int currentFrameCount = timetableFrameRepository.countByUserIdAndSemesterId(userId, semester.getId()) + 1;
        boolean isMain = currentFrameCount == 1;

        TimeTableFrame timeTableFrame = request.toTimetablesFrame(user, semester, "시간표" + currentFrameCount, isMain);
        return TimeTableFrameResponse.from(timetableFrameRepository.save(timeTableFrame));
    }

    public List<TimeTableFrameResponse> getTimetablesFrame(Integer userId, String semesterRequest) {
        Semester semester = semesterRepository.getBySemester(semesterRequest);
        return timetableFrameRepository.findAllByUserIdAndSemesterId(userId, semester.getId()).stream()
            .map(TimeTableFrameResponse::from)
            .toList();
    }

    @Transactional
    public void deleteTimetablesFrame(Integer userId, Integer frameId) {
        TimeTableFrame frame = timetableFrameRepository.getById(frameId);
        if (!Objects.equals(frame.getUser().getId(), userId)) {
            throw AuthorizationException.withDetail("userId: " + userId);
        }
        if(frame.isMain()) {
            TimeTableFrame nextMainFrame =
                timetableFrameRepository.
                    findFirstByUserIdAndSemesterIdAndIsMainFalseOrderByCreatedAtAsc(userId, frame.getSemester().getId());
            if (nextMainFrame != null) {
                nextMainFrame.updateStatusMain(true);
                timetableFrameRepository.save(nextMainFrame);
            }
        }
        timetableFrameRepository.deleteById(frameId);
    }

    public TimeTableResponse getTimeTables(Integer userId, String semesterRequest) {
        Semester semester = semesterRepository.getBySemester(semesterRequest);
        return getTimeTableResponse(userId, semester);
    }

    @Transactional
    public TimeTableResponse createTimeTables(Integer userId, TimeTableCreateRequest request) {
        User user = userRepository.getById(userId);
        Semester semester = semesterRepository.getBySemester(request.semester());
        for (TimeTableCreateRequest.InnerTimeTableRequest timeTableRequest : request.timetable()) {
            TimeTable timeTable = timeTableRequest.toTimeTable(user, semester);
            timeTableRepository.save(timeTable);
        }
        return getTimeTableResponse(userId, semester);
    }

    @Transactional
    public TimeTableResponse updateTimeTables(Integer userId, TimeTableUpdateRequest request) {
        Semester semester = semesterRepository.getBySemester(request.semester());
        for (TimeTableUpdateRequest.InnerTimeTableRequest timeTableRequest : request.timetable()) {
            TimeTable timeTable = timeTableRepository.getById(timeTableRequest.id());
            timeTable.update(timeTableRequest);
        }
        return getTimeTableResponse(userId, semester);
    }

    @Transactional
    public void deleteTimeTable(Integer id) {
        TimeTable timeTable = timeTableRepository.getById(id);
        timeTable.updateIsDeleted(true);
    }

    private TimeTableResponse getTimeTableResponse(Integer userId, Semester semester) {
        List<TimeTable> timeTables = timeTableRepository.findAllByUserIdAndSemesterId(userId, semester.getId());
        Integer grades = timeTables.stream()
            .mapToInt(timeTable -> Integer.parseInt(timeTable.getGrades()))
            .sum();
        Integer totalGrades = calculateTotalGrades(userId);

        return TimeTableResponse.of(semester.getSemester(), timeTables, grades, totalGrades);
    }

    private int calculateTotalGrades(Integer userId) {
        int totalGrades = 0;
        List<Semester> semesters = semesterRepository.findAllByOrderBySemesterDesc();

        for (Semester semester : semesters) {
            totalGrades += timeTableRepository.findAllByUserIdAndSemesterId(userId, semester.getId()).stream()
                .mapToInt(timeTable -> Integer.parseInt(timeTable.getGrades()))
                .sum();
        }

        return totalGrades;
    }
}
