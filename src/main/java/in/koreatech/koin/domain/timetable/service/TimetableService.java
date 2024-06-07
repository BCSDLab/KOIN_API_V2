package in.koreatech.koin.domain.timetable.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.dto.LectureResponse;
import in.koreatech.koin.domain.timetable.dto.TimeTableCreateRequest;
import in.koreatech.koin.domain.timetable.dto.TimeTableResponse;
import in.koreatech.koin.domain.timetable.dto.TimeTableUpdateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableFrameUpdateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableFrameUpdateResponse;
import in.koreatech.koin.domain.timetable.exception.SemesterNotFoundException;
import in.koreatech.koin.domain.timetable.exception.TimetableLectureNotFoundException;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetable.model.TimeTable;
import in.koreatech.koin.domain.timetable.model.TimeTableFrame;
import in.koreatech.koin.domain.timetable.model.TimeTableLecture;
import in.koreatech.koin.domain.timetable.repository.LectureRepository;
import in.koreatech.koin.domain.timetable.repository.SemesterRepository;
import in.koreatech.koin.domain.timetable.repository.TimeTableFrameRepository;
import in.koreatech.koin.domain.timetable.repository.TimeTableLectureRepository;
import in.koreatech.koin.domain.timetable.repository.TimeTableRepository;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimetableService {

    private final LectureRepository lectureRepository;
    private final SemesterRepository semesterRepository;
    private final TimeTableRepository timeTableRepository;
    private final TimeTableLectureRepository timeTableLectureRepository;
    private final TimeTableFrameRepository timeTableFrameRepository;
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

    private TimeTableResponse getTimeTableResponse(Integer userId, Semester semester) {
        TimeTableFrame mainTimetableFrame = findMainTimetable(userId, semester.getId());
        List<TimeTableLecture> timetableLecture = timeTableLectureRepository.findAllByTimetableFrameId(mainTimetableFrame.getId());

        List<Integer> lectureIds = timetableLecture.stream().map(tl -> tl.getLecture().getId()).toList();
        List<Lecture> lectures = lectureRepository.findAllByIds(lectureIds);

        Integer grades = lectures.stream()
            .mapToInt(lecture -> Integer.parseInt(lecture.getGrades()))
            .sum();
        Integer totalGrades = calculateTotalGrades(userId);

        return TimeTableResponse.of(semester.getSemester(), timetableLecture, lectures, grades, totalGrades);
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

    @Transactional
    public void deleteTimetableLecture(Integer id) {
        timeTableLectureRepository.getById(id);
        timeTableLectureRepository.deleteById(id);
    }

    @Transactional
    public TimetableFrameUpdateResponse updateTimeTableFrame(Integer timetableFrameId,
        TimetableFrameUpdateRequest timetableFrameUpdateRequest, Integer userId) {
        TimeTableFrame timeTableFrame = timeTableFrameRepository.getById(timetableFrameId);
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
        TimeTableFrame mainTimetableFrame = findMainTimetable(userId, semesterId);
        mainTimetableFrame.cancelMain();
    }

    private TimeTableFrame findMainTimetable(Integer userId, Integer semesterId) {
        return timeTableFrameRepository.findAllByUserIdAndSemesterId(userId, semesterId)
            .stream()
            .filter(TimeTableFrame::isMain)
            .findFirst()
            .orElseThrow(() -> TimetableLectureNotFoundException.withDetail("못 찾음 userId: "+ userId + ", semesterId: " + semesterId));
    }
}
