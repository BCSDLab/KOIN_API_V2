package in.koreatech.koin.domain.timetable.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.dto.LectureResponse;
import in.koreatech.koin.domain.timetable.dto.SemesterCheckResponse;
import in.koreatech.koin.domain.timetable.dto.TimeTableCreateRequest;
import in.koreatech.koin.domain.timetable.dto.TimeTableResponse;
import in.koreatech.koin.domain.timetable.dto.TimeTableUpdateRequest;
import in.koreatech.koin.domain.timetable.exception.SemesterNotFoundException;
import in.koreatech.koin.domain.timetable.exception.StudentSemesterNotFoundException;
import in.koreatech.koin.domain.timetable.model.Lecture;
import in.koreatech.koin.domain.timetable.model.Semester;
import in.koreatech.koin.domain.timetable.model.TimeTable;
import in.koreatech.koin.domain.timetable.repository.LectureRepository;
import in.koreatech.koin.domain.timetable.repository.SemesterRepository;
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

    @Transactional
    public void deleteTimeTable(Integer id) {
        TimeTable timeTable = timeTableRepository.getById(id);
        timeTable.updateIsDeleted(true);
    }

    public SemesterCheckResponse getCheckSemesters(Integer userId) {
        List<TimeTable> timeTables = timeTableRepository.findAllByUserId(userId);
        if (timeTables.isEmpty()) {
            throw StudentSemesterNotFoundException.withDetail("학생의 학기 정보가 없습니다.");
        }
        return SemesterCheckResponse.of(userId, timeTables);
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
