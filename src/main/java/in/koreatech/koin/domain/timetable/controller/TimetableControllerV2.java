package in.koreatech.koin.domain.timetable.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.timetable.dto.TimetableFrameUpdateRequest;
import in.koreatech.koin.domain.timetable.dto.TimetableFrameUpdateResponse;
import in.koreatech.koin.domain.timetable.service.TimetableService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TimetableControllerV2 implements TimetableApiV2 {

    private final TimetableService timetableService;

    @DeleteMapping("V2/timetables/lecture/{id}")
    public ResponseEntity<Void> deleteTimetableLecture(
        @PathVariable(value = "id") Integer timetableLectureId,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        timetableService.deleteTimetableLecture(timetableLectureId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("V2/timetables/frame/{id}")
    public ResponseEntity<TimetableFrameUpdateResponse> updateTimetableFrame(
        @PathVariable(value = "id") Integer timetableFrameId,
        @Valid @RequestBody TimetableFrameUpdateRequest timetableFrameUpdateRequest,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        TimetableFrameUpdateResponse timetableFrameUpdateResponse =
            timetableService.updateTimeTableFrame(timetableFrameId, timetableFrameUpdateRequest, userId);
        return ResponseEntity.ok(timetableFrameUpdateResponse);
    }
}
