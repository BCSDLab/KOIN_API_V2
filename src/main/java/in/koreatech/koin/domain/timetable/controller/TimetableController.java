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
import in.koreatech.koin.domain.timetable.dto.TimetableCreateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableResponse;
import in.koreatech.koin.domain.timetable.dto.TimetableUpdateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableFrameCreateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableFrameResponse;
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

    @PostMapping("/timetables/frame")
    public ResponseEntity<TimetableFrameResponse> createTimetablesFrame(
        @Valid @RequestBody TimetableFrameCreateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableFrameResponse response = timetableService.createTimetablesFrame(userId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/timetables/frames")
    public ResponseEntity<List<TimetableFrameResponse>> getTimetablesFrame(
        @RequestParam(name = "semester") String semester,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        List<TimetableFrameResponse> timeTableFrameResponse = timetableService.getTimetablesFrame(userId, semester);
        return ResponseEntity.ok(timeTableFrameResponse);
    }

    @DeleteMapping("/timetables/frame")
    public ResponseEntity<Void> deleteTimetablesFrame(
        @RequestParam(name = "id") Integer frameId,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        timetableService.deleteTimetablesFrame(userId, frameId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/timetables")
    public ResponseEntity<TimetableResponse> getTimetables(
        @RequestParam(name = "semester") String semester,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableResponse timetableResponse = timetableService.getTimetables(userId, semester);
        return ResponseEntity.ok(timetableResponse);
    }

    @PostMapping("/timetables")
    public ResponseEntity<TimetableResponse> createTimetables(
        @Valid @RequestBody TimetableCreateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableResponse timetableResponse = timetableService.createTimetables(userId, request);
        return ResponseEntity.ok(timetableResponse);
    }

    @PutMapping("/timetables")
    public ResponseEntity<TimetableResponse> updateTimetable(
        @Valid @RequestBody TimetableUpdateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableResponse timetableResponse = timetableService.updateTimetables(userId, request);
        return ResponseEntity.ok(timetableResponse);
    }

    @DeleteMapping("/timetable")
    public ResponseEntity<Void> deleteTimetableById(
        @RequestParam(name = "id") Integer timetableId,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        timetableService.deleteTimetableLecture(userId, timetableId);
        return ResponseEntity.ok().build();
    }
}
