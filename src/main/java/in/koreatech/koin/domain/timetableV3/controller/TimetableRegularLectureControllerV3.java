package in.koreatech.koin.domain.timetableV3.controller;

import static in.koreatech.koin.domain.user.model.UserType.COUNCIL;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.timetableV3.dto.request.TimetableRegularLectureCreateRequest;
import in.koreatech.koin.domain.timetableV3.dto.request.TimetableRegularLectureUpdateRequest;
import in.koreatech.koin.domain.timetableV3.dto.response.TimetableLectureResponseV3;
import in.koreatech.koin.domain.timetableV3.service.TimetableRegularLectureServiceV3;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TimetableRegularLectureControllerV3 implements TimetableRegularLectureApiV3 {

    private final TimetableRegularLectureServiceV3 timetableLectureV3;

    @PostMapping("/v3/timetables/lecture/regular")
    public ResponseEntity<TimetableLectureResponseV3> createTimetablesRegularLecture(
        @Valid @RequestBody TimetableRegularLectureCreateRequest request,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        TimetableLectureResponseV3 response = timetableLectureV3.createTimetablesRegularLecture(request, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/v3/timetables/lecture/regular")
    public ResponseEntity<TimetableLectureResponseV3> updateTimetablesRegularLecture(
        @Valid @RequestBody TimetableRegularLectureUpdateRequest request,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        TimetableLectureResponseV3 response = timetableLectureV3.updateTimetablesRegularLecture(request, userId);
        return ResponseEntity.ok(response);
    }
}
