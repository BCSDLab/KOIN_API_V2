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
import in.koreatech.koin.domain.timetable.dto.TimetableLectureResponse;
import in.koreatech.koin.domain.timetable.dto.TimetableLectureUpdateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableResponse;
import in.koreatech.koin.domain.timetable.dto.TimetableUpdateRequest;
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

    /*
    @GetMapping("/timetables")
    public ResponseEntity<TimetableResponse> getTimeTables(
        @RequestParam(name = "semester") String semester,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableResponse timeTableResponse = timetableService.getTimeTables(userId, semester);
        return ResponseEntity.ok(timeTableResponse);
    }

    @PostMapping("/timetables")
    public ResponseEntity<TimetableResponse> createTimeTables(
        @Valid @RequestBody TimeTableCreateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableResponse timeTableResponse = timetableService.createTimeTables(userId, request);
        return ResponseEntity.ok(timeTableResponse);
    }

    */

    @PutMapping("/timetables")
    public ResponseEntity<TimetableResponse> updateTimeTable(
        @Valid @RequestBody TimetableUpdateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableResponse timeTableResponse = timetableService.updateTimetables(userId, request);
        return ResponseEntity.ok(timeTableResponse);
    }

    @PutMapping("/v2/timetables")
    public ResponseEntity<TimetableLectureResponse> updateTimeTableLecture(
        @Valid @RequestBody TimetableLectureUpdateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableLectureResponse timetableLectureResponse = timetableService.updateTimetablesLectures(userId, request);
        return ResponseEntity.ok(timetableLectureResponse);
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
