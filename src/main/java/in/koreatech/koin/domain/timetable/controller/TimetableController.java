package in.koreatech.koin.domain.timetable.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.timetable.dto.SemesterCheckResponse;
import in.koreatech.koin.domain.timetable.dto.TimetableCreateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableLectureCreateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableResponse;
import in.koreatech.koin.domain.timetable.dto.TimetableLectureResponse;
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

    @GetMapping("/semesters/check")
    public ResponseEntity<SemesterCheckResponse> getStudentSemesters(
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        SemesterCheckResponse semesterCheckResponse = semesterService.getStudentSemesters(userId);
        return ResponseEntity.ok(semesterCheckResponse);
    }

    @GetMapping("/timetables")
    public ResponseEntity<TimetableResponse> getTimetables(
        @RequestParam(name = "semester") String semester,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableResponse timeTableResponse = timetableService.getTimetables(userId, semester);
        return ResponseEntity.ok(timeTableResponse);
    }

    @PostMapping("/timetables")
    public ResponseEntity<TimetableResponse> createTimetables(
        @Valid @RequestBody TimetableCreateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableResponse timeTableResponse = timetableService.createTimetables(userId, request);
        return ResponseEntity.ok(timeTableResponse);
    }

    @PostMapping("/V2/timetables/lecture")
    public ResponseEntity<TimetableLectureResponse> createTimetableLecture(
        @Valid @RequestBody TimetableLectureCreateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableLectureResponse timeTableLectureResponse = timetableService.createTimetableLectures(userId, request);
        return ResponseEntity.ok(timeTableLectureResponse);
    }
}
