package in.koreatech.koin.domain.timetable.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.timetable.service.TimetableLectureService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class TimetableControllerV2 implements TimetableApiV2 {

    private final TimetableLectureService timetableLectureService;

    @DeleteMapping("V2/timetable")
    public ResponseEntity<Void> deleteTimetableLecture(
        @RequestParam(name = "id") Integer id,
        @Auth(permit = {STUDENT}) Integer userId
    ) {
        timetableLectureService.deleteTimetableLecture(id);
        return ResponseEntity.ok().build();
    }
}
