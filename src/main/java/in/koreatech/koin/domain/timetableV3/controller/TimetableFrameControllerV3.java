package in.koreatech.koin.domain.timetableV3.controller;

import static in.koreatech.koin.domain.user.model.UserType.COUNCIL;
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

import in.koreatech.koin.domain.timetableV3.dto.request.TimetableFrameCreateRequestV3;
import in.koreatech.koin.domain.timetableV3.dto.request.TimetableFrameUpdateRequestV3;
import in.koreatech.koin.domain.timetableV3.dto.response.TimetableFrameResponseV3;
import in.koreatech.koin.domain.timetableV3.dto.response.TimetableFramesResponseV3;
import in.koreatech.koin.domain.timetableV3.service.TimetableFrameServiceV3;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TimetableFrameControllerV3 implements TimetableFrameApiV3 {

    private final TimetableFrameServiceV3 timetableFrameServiceV3;

    @PostMapping("/v3/timetables/frame")
    public ResponseEntity<List<TimetableFrameResponseV3>> createTimetablesFrame(
        @Valid @RequestBody TimetableFrameCreateRequestV3 request,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        List<TimetableFrameResponseV3> response = timetableFrameServiceV3.createTimetablesFrame(request, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/v3/timetables/frame/{id}")
    public ResponseEntity<List<TimetableFrameResponseV3>> updateTimetableFrame(
        @Valid @RequestBody TimetableFrameUpdateRequestV3 request,
        @PathVariable(value = "id") Integer timetableFrameId,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        List<TimetableFrameResponseV3> response = timetableFrameServiceV3.updateTimetableFrame(request,
            timetableFrameId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/v3/timetables/frame")
    public ResponseEntity<List<TimetableFrameResponseV3>> getTimetablesFrame(
        @RequestParam(name = "year") Integer year,
        @RequestParam(name = "term") String term,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        List<TimetableFrameResponseV3> response = timetableFrameServiceV3.getTimetablesFrame(year, term, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/v3/timetables/frames")
    public ResponseEntity<List<TimetableFramesResponseV3>> getTimetablesFrames(
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        List<TimetableFramesResponseV3> response = timetableFrameServiceV3.getTimetablesFrames(userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/v3/timetables/frames")
    public ResponseEntity<Void> deleteTimetablesFrames(
        @RequestParam(name = "year") Integer year,
        @RequestParam(name = "term") String term,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        timetableFrameServiceV3.deleteTimetablesFrames(year, term, userId);
        return ResponseEntity.noContent().build();
    }
}
