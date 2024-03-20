package in.koreatech.koin.domain.timetable.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.koreatech.koin.domain.timetable.dto.LectureResponse;
import in.koreatech.koin.domain.timetable.dto.TimeTableRequest;
import in.koreatech.koin.domain.timetable.dto.TimeTableResponse;
import in.koreatech.koin.domain.timetable.dto.TimeTableUpdateRequest;
import in.koreatech.koin.domain.timetable.exception.SemesterNotFoundException;
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

    public List<TimeTableResponse> getTimeTables(Long userId, String semester) {
        Semester semesterEntity = semesterRepository.getBySemester(semester);
        List<TimeTable> timeTables = timeTableRepository.getByUserIdAndSemesterId(userId, semesterEntity.getId());

        return timeTables.stream()
            .map(TimeTableResponse::from)
            .toList();
    }

    @Transactional
    public List<TimeTableResponse> createTimeTables(Long userId, TimeTableRequest request) {
        User user = userRepository.getById(userId);
        Semester semester = semesterRepository.getBySemester(request.semester());

        for (TimeTableRequest.InnerTimeTableRequest timeTableRequest : request.timetable()) {
            TimeTable timeTable = TimeTableRequest.toTimeTable(user, semester, timeTableRequest);
            timeTableRepository.save(timeTable);
        }

        return getTimeTables(userId, semester.getSemester());
    }

    @Transactional
    public List<TimeTableResponse> updateTimeTables(Long userId, TimeTableUpdateRequest request) {
        Semester semester = semesterRepository.getBySemester(request.semester());

        for (TimeTableUpdateRequest.InnerTimeTableRequest timeTableRequest : request.timetable()) {
            TimeTable timeTable = timeTableRepository.getById(timeTableRequest.id());
            timeTable.update(timeTableRequest);
        }

        return getTimeTables(userId, semester.getSemester());
    }

    @Transactional
    public void deleteTimeTable(Long id) {
        TimeTable timeTable = timeTableRepository.getById(id);
        timeTable.updateIsDeleted(true);
    }
}
