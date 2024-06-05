package in.koreatech.koin.domain.timetable.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.timetable.dto.LectureResponse;
import in.koreatech.koin.domain.timetable.dto.SemesterCheckResponse;
import in.koreatech.koin.domain.timetable.dto.SemesterResponse;
import in.koreatech.koin.domain.timetable.dto.TimeTableCreateRequest;
import in.koreatech.koin.domain.timetable.dto.TimeTableResponse;
import in.koreatech.koin.domain.timetable.dto.TimeTableUpdateRequest;
import in.koreatech.koin.domain.timetable.dto.TimeTableFrameRequest;
import in.koreatech.koin.domain.timetable.dto.TimeTableFrameResponse;
import in.koreatech.koin.domain.timetable.service.SemesterService;
import in.koreatech.koin.domain.timetable.service.TimetableService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
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

    @GetMapping("/semesters/check")
    public ResponseEntity<SemesterCheckResponse> getStudentSemesters(
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        SemesterCheckResponse semesterCheckResponse = semesterService.getStudentSemesters(userId);
        return ResponseEntity.ok(semesterCheckResponse);
    }

    @PostMapping("/timetalbes/frame")
    public ResponseEntity<TimeTableFrameResponse> createTimetablesFrame(
        @Valid @RequestBody TimeTableFrameRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimeTableFrameResponse response = timetableService.createTimetablesFrame(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/timetables/frame")
    public ResponseEntity<List<TimeTableFrameResponse>> getTimetablesFrame(
        @RequestParam(name = "semester") String semester,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        List<TimeTableFrameResponse> timeTableFrameRespons = timetableService.getTimetablesFrame(userId, semester);
        return ResponseEntity.ok(timeTableFrameRespons);
    }

    @DeleteMapping("/timetalbes/frame")
    public ResponseEntity<Void> deleteTimetablesFrame(
        @RequestParam(name = "id") Integer id,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        timetableService.deleteTimetablesFrame(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/timetables")
    public ResponseEntity<TimeTableResponse> getTimeTables(
        @RequestParam(name = "semester") String semester,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimeTableResponse timeTableResponse = timetableService.getTimeTables(userId, semester);
        return ResponseEntity.ok(timeTableResponse);
    }

    @PostMapping("/timetables")
    public ResponseEntity<TimeTableResponse> createTimeTables(
        @Valid @RequestBody TimeTableCreateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimeTableResponse timeTableResponse = timetableService.createTimeTables(userId, request);
        return ResponseEntity.ok(timeTableResponse);
    }

    @PutMapping("/timetables")
    public ResponseEntity<TimeTableResponse> updateTimeTable(
        @Valid @RequestBody TimeTableUpdateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimeTableResponse timeTableResponse = timetableService.updateTimeTables(userId, request);
        return ResponseEntity.ok(timeTableResponse);
    }

    @DeleteMapping("/timetable")
    public ResponseEntity<Void> deleteTimeTableById(
        @RequestParam(name = "id") Integer id,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        timetableService.deleteTimeTable(id);
        return ResponseEntity.ok().build();
    }
}
