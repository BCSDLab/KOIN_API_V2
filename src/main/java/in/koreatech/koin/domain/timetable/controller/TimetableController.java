package in.koreatech.koin.domain.timetable.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.timetable.dto.SemesterCheckResponse;
import in.koreatech.koin.domain.timetable.dto.TimeTableCreateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableLectureCreateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableResponse;
import in.koreatech.koin.domain.timetable.dto.TimetableLectureResponse;
import in.koreatech.koin.domain.timetable.service.SemesterService;
import in.koreatech.koin.domain.timetable.service.TimetableLectureService;
import in.koreatech.koin.domain.timetable.service.TimetableService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TimetableController implements TimetableApi {

    private final TimetableService timetableService;
    private final TimetableLectureService timetableLectureService;
    private final SemesterService semesterService;

    @GetMapping("/semesters/check")
    public ResponseEntity<SemesterCheckResponse> getStudentSemesters(
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        SemesterCheckResponse semesterCheckResponse = semesterService.getStudentSemesters(userId);
        return ResponseEntity.ok(semesterCheckResponse);
    }

    @PostMapping("/timetables")
    public ResponseEntity<TimetableResponse> createTimeTables(
        @Valid @RequestBody TimeTableCreateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableResponse timeTableResponse = timetableService.createTimeTables(userId, request);
        return ResponseEntity.ok(timeTableResponse);
    }

    @PostMapping("/v2/timetables/lecture")
    public ResponseEntity<TimetableLectureResponse> createTimetableLecture(
        @Valid @RequestBody TimetableLectureCreateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableLectureResponse timeTableLectureResponse = timetableLectureService.createTimetableLectures(userId, request);
        return ResponseEntity.ok(timeTableLectureResponse);
    }
}
