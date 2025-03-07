package in.koreatech.koin.domain.timetableV3.controller;

import static in.koreatech.koin.domain.user.model.UserType.COUNCIL;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.timetableV3.dto.response.TakeAllTimetableLectureResponse;
import in.koreatech.koin.domain.timetableV3.dto.response.TimetableLectureResponseV3;
import in.koreatech.koin.domain.timetableV3.service.TimetableLectureServiceV3;
import in.koreatech.koin._common.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TimetableLectureControllerV3 implements TimetableLectureApiV3 {

    private final TimetableLectureServiceV3 lectureServiceV3;

    @GetMapping("/v3/timetables/lecture")
    public ResponseEntity<TimetableLectureResponseV3> getTimetableLecture(
        @RequestParam(value = "timetable_frame_id") Integer timetableFrameId,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        TimetableLectureResponseV3 response = lectureServiceV3.getTimetableLecture(timetableFrameId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/v3/timetables/main/lectures")
    public ResponseEntity<TakeAllTimetableLectureResponse> getTakeAllTimetableLectures(
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        TakeAllTimetableLectureResponse response = lectureServiceV3.getTakeAllTimetableLectures(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/v3/timetables/lecture/rollback")
    public ResponseEntity<TimetableLectureResponseV3> rollbackTimetableLecture(
        @RequestParam(name = "timetable_lectures_id") List<Integer> timetableLecturesId,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        TimetableLectureResponseV3 response = lectureServiceV3.rollbackTimetableLecture(timetableLecturesId, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/v3/timetables/frame/rollback")
    public ResponseEntity<TimetableLectureResponseV3> rollbackTimetableFrame(
        @RequestParam(name = "timetable_frame_id") Integer timetableFrameId,
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        TimetableLectureResponseV3 response = lectureServiceV3.rollbackTimetableFrame(timetableFrameId, userId);
        return ResponseEntity.ok(response);
    }
}
