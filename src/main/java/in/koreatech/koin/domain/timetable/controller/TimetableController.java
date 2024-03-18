package in.koreatech.koin.domain.timetable.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.timetable.dto.LectureResponse;
import in.koreatech.koin.domain.timetable.dto.SemesterResponse;
import in.koreatech.koin.domain.timetable.dto.TimeTableResponse;
import in.koreatech.koin.domain.timetable.service.SemesterService;
import in.koreatech.koin.domain.timetable.service.TimetableService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TimetableController implements TimetableApi {

    private final TimetableService timetableService;
    private final SemesterService semesterService;

    @GetMapping("/lectures")
    public ResponseEntity<List<LectureResponse>> getLecture(
        @RequestParam(name = "semester_date") String semester
    ) {
        List<LectureResponse> lectures = timetableService.getLecturesBySemester(semester);
        return ResponseEntity.ok(lectures);
    }

    @GetMapping("/semesters")
    public ResponseEntity<List<SemesterResponse>> getSemesters() {
        List<SemesterResponse> semesterResponse = semesterService.getSemesters();
        return ResponseEntity.ok(semesterResponse);
    }

    @GetMapping("/timetables")
    public ResponseEntity<List<TimeTableResponse>> getTimeTables(
        @RequestParam(name = "semester") String semester,
        @Auth(permit = {STUDENT}) Long userId
    ) {
        List<TimeTableResponse> timeTableResponse = timetableService.getTimeTables(userId, semester);
        return ResponseEntity.ok(timeTableResponse);
    }
}
