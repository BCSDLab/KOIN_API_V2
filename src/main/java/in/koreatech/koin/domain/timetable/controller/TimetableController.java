package in.koreatech.koin.domain.timetable.controller;

import static in.koreatech.koin.domain.user.model.UserType.COUNCIL;
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
import in.koreatech.koin.domain.timetable.service.SemesterService;
import in.koreatech.koin.domain.timetable.service.TimetableService;
import in.koreatech.koin._common.auth.Auth;
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
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        SemesterCheckResponse semesterCheckResponse = semesterService.getStudentSemesters(userId);
        return ResponseEntity.ok(semesterCheckResponse);
    }

    @GetMapping("/timetables")
    public ResponseEntity<TimetableResponse> getTimetables(
        @RequestParam(name = "semester") String semester,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        TimetableResponse timetableResponse = timetableService.getTimetables(userId, semester);
        return ResponseEntity.ok(timetableResponse);
    }

    @PostMapping("/timetables")
    public ResponseEntity<TimetableResponse> createTimetables(
        @Valid @RequestBody TimetableCreateRequest request,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        TimetableResponse timeTableResponse = timetableService.createTimetables(userId, request);
        return ResponseEntity.ok(timeTableResponse);
    }

    @PutMapping("/timetables")
    public ResponseEntity<TimetableResponse> updateTimetable(
        @Valid @RequestBody TimetableUpdateRequest request,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        TimetableResponse timetableResponse = timetableService.updateTimetables(userId, request);
        return ResponseEntity.ok(timetableResponse);
    }

    @DeleteMapping("/timetable")
    public ResponseEntity<Void> deleteTimetableById(
        @RequestParam(name = "id") Integer timetableId,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        timetableService.deleteTimetableLecture(userId, timetableId);
        return ResponseEntity.ok().build();
    }
}
