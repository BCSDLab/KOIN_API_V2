package in.koreatech.koin.domain.timetable.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.timetable.dto.TimetableFrameCreateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableFrameResponse;
import in.koreatech.koin.domain.timetable.dto.TimetableFrameUpdateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableFrameUpdateResponse;
import in.koreatech.koin.domain.timetable.dto.TimetableLectureCreateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableLectureResponse;
import in.koreatech.koin.domain.timetable.dto.TimetableLectureUpdateRequest;
import in.koreatech.koin.domain.timetable.service.TimetableService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TimetableControllerV2 implements TimetableApiV2 {

    private final TimetableService timetableService;

    @PostMapping("/timetables/frame")
    public ResponseEntity<TimetableFrameResponse> createTimetablesFrame(
        @Valid @RequestBody TimetableFrameCreateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableFrameResponse response = timetableService.createTimetablesFrame(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/timetables/frame/{id}")
    public ResponseEntity<TimetableFrameUpdateResponse> updateTimetableFrame(
        @PathVariable(value = "id") Integer timetableFrameId,
        @Valid @RequestBody TimetableFrameUpdateRequest timetableFrameUpdateRequest,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableFrameUpdateResponse timetableFrameUpdateResponse =
            timetableService.updateTimetableFrame(timetableFrameId, timetableFrameUpdateRequest, userId);
        return ResponseEntity.ok(timetableFrameUpdateResponse);
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

    @PostMapping("/V2/timetables/lecture")
    public ResponseEntity<TimetableLectureResponse> createTimetableLecture(
        @Valid @RequestBody TimetableLectureCreateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableLectureResponse timeTableLectureResponse = timetableService.createTimetableLectures(userId, request);
        return ResponseEntity.ok(timeTableLectureResponse);
    }

    @PutMapping("/V2/timetables/lecture")
    public ResponseEntity<TimetableLectureResponse> updateTimetableLecture(
        @Valid @RequestBody TimetableLectureUpdateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableLectureResponse timetableLectureResponse = timetableService.updateTimetablesLectures(userId, request);
        return ResponseEntity.ok(timetableLectureResponse);
    }

    @DeleteMapping("/V2/timetables/lecture/{id}")
    public ResponseEntity<Void> deleteTimetableLecture(
        @PathVariable(value = "id") Integer timetableLectureId,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        timetableService.deleteTimetableLecture(userId, timetableLectureId);
        return ResponseEntity.ok().build();
    }
}
