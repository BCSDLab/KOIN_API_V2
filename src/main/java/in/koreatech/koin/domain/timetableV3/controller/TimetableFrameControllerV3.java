package in.koreatech.koin.domain.timetableV3.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.timetableV3.dto.request.TimetableFrameCreateRequestV3;
import in.koreatech.koin.domain.timetableV3.dto.response.TimetableFrameResponseV3;
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
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        List<TimetableFrameResponseV3> response = timetableFrameServiceV3.createTimetablesFrame(request, userId);
        return ResponseEntity.ok(response);
    }
}
