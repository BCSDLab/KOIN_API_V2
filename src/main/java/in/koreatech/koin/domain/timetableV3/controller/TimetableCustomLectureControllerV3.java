package in.koreatech.koin.domain.timetableV3.controller;

import static in.koreatech.koin.domain.user.model.UserType.COUNCIL;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.timetableV3.dto.request.TimetableCustomLectureCreateRequest;
import in.koreatech.koin.domain.timetableV3.dto.request.TimetableCustomLectureUpdateRequest;
import in.koreatech.koin.domain.timetableV3.dto.response.TimetableLectureResponseV3;
import in.koreatech.koin.domain.timetableV3.service.TimetableCustomLectureServiceV3;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TimetableCustomLectureControllerV3 implements TimetableCustomLectureApiV3 {

    private final TimetableCustomLectureServiceV3 customLectureServiceV3;

    @PostMapping("/v3/timetables/lecture/custom")
    public ResponseEntity<TimetableLectureResponseV3> createTimetablesCustomLecture(
        @Valid @RequestBody TimetableCustomLectureCreateRequest request,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        TimetableLectureResponseV3 response = customLectureServiceV3.createTimetablesCustomLecture(request, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/v3/timetables/lecture/custom")
    public ResponseEntity<TimetableLectureResponseV3> updateTimetablesCustomLecture(
        @Valid @RequestBody TimetableCustomLectureUpdateRequest request,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        TimetableLectureResponseV3 response = customLectureServiceV3.updateTimetablesCustomLecture(request, userId);
        return ResponseEntity.ok(response);
    }
}
