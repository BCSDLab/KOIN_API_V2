package in.koreatech.koin.domain.timetableV2.controller;

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

import in.koreatech.koin.domain.timetableV2.dto.TimetableFrameCreateRequest;
import in.koreatech.koin.domain.timetableV2.dto.TimetableFrameResponse;
import in.koreatech.koin.domain.timetableV2.dto.TimetableFrameUpdateRequest;
import in.koreatech.koin.domain.timetableV2.dto.TimetableFrameUpdateResponse;
import in.koreatech.koin.domain.timetableV2.dto.TimetableLectureCreateRequest;
import in.koreatech.koin.domain.timetableV2.dto.TimetableLectureResponse;
import in.koreatech.koin.domain.timetableV2.dto.TimetableLectureUpdateRequest;
import in.koreatech.koin.domain.timetableV2.service.TimetableServiceV2;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TimetableControllerV2 implements TimetableApiV2 {

    private final TimetableServiceV2 timetableServiceV2;

    @PostMapping("/v2/timetables/frame")
    public ResponseEntity<TimetableFrameResponse> createTimetablesFrame(
        @Valid @RequestBody TimetableFrameCreateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableFrameResponse response = timetableServiceV2.createTimetablesFrame(userId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/v2/timetables/frame/{id}")
    public ResponseEntity<TimetableFrameUpdateResponse> updateTimetableFrame(
        @PathVariable(value = "id") Integer timetableFrameId,
        @Valid @RequestBody TimetableFrameUpdateRequest timetableFrameUpdateRequest,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableFrameUpdateResponse timetableFrameUpdateResponse =
            timetableServiceV2.updateTimetableFrame(timetableFrameId, timetableFrameUpdateRequest, userId);
        return ResponseEntity.ok(timetableFrameUpdateResponse);
    }

    @GetMapping("/v2/timetables/frames")
    public ResponseEntity<List<TimetableFrameResponse>> getTimetablesFrame(
        @RequestParam(name = "semester") String semester,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        List<TimetableFrameResponse> timeTableFrameResponse = timetableServiceV2.getTimetablesFrame(userId, semester);
        return ResponseEntity.ok(timeTableFrameResponse);
    }

    @DeleteMapping("/v2/timetables/frame")
    public ResponseEntity<Void> deleteTimetablesFrame(
        @RequestParam(name = "id") Integer frameId,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        timetableServiceV2.deleteTimetablesFrame(userId, frameId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/v2/all/timetables/frame")
    public ResponseEntity<Void> deleteAllTimetablesFrame(
        @RequestParam(name = "semester") String semester,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        timetableServiceV2.deleteAllTimetablesFrame(userId, semester);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/v2/timetables/lecture")
    public ResponseEntity<TimetableLectureResponse> createTimetableLecture(
        @Valid @RequestBody TimetableLectureCreateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableLectureResponse timeTableLectureResponse = timetableServiceV2.createTimetableLectures(userId, request);
        return ResponseEntity.ok(timeTableLectureResponse);
    }

    @PutMapping("/v2/timetables/lecture")
    public ResponseEntity<TimetableLectureResponse> updateTimetableLecture(
        @Valid @RequestBody TimetableLectureUpdateRequest request,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableLectureResponse timetableLectureResponse = timetableServiceV2.updateTimetablesLectures(userId, request);
        return ResponseEntity.ok(timetableLectureResponse);
    }

    @GetMapping("/v2/timetables/lecture")
    public ResponseEntity<TimetableLectureResponse> getTimetableLecture(
        @RequestParam(name = "timetable_frame_id") Integer timetableFrameId,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableLectureResponse timetableLectureResponse = timetableServiceV2.getTimetableLectures(userId,
            timetableFrameId);
        return ResponseEntity.ok(timetableLectureResponse);
    }

    @DeleteMapping("/v2/timetables/lecture/{id}")
    public ResponseEntity<Void> deleteTimetableLecture(
        @PathVariable(value = "id") Integer timetableLectureId,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        timetableServiceV2.deleteTimetableLecture(userId, timetableLectureId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/v2/timetables/frame/{frameId}/lecture/{lectureId}")
    public ResponseEntity<Void> deleteTimetableLectureByFrameId(
        @PathVariable(value = "frameId") Integer frameId,
        @PathVariable(value = "lectureId") Integer lectureId,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        timetableServiceV2.deleteTimetableLectureByFrameId(frameId, lectureId, userId);
        return ResponseEntity.noContent().build();
    }
}
