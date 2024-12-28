package in.koreatech.koin.domain.timetableV3.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.timetableV3.dto.response.LectureResponseV3;
import in.koreatech.koin.domain.timetableV3.service.LectureServiceV3;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LectureControllerV3 implements LectureApiV3 {

    private final LectureServiceV3 lectureServiceV3;

    // TODO. Semester v3 올라가면 파라미터 변경하기
    @GetMapping("/v3/lectures")
    public ResponseEntity<List<LectureResponseV3>> getLectures(
        String semesterDate
    ) {
        List<LectureResponseV3> response = lectureServiceV3.getLectures(semesterDate);
        return ResponseEntity.ok(response);
    }
}
