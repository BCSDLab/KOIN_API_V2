package in.koreatech.koin.domain.timetable.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.timetable.dto.LectureResponse;
import in.koreatech.koin.domain.timetable.service.LectureService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class LectureController implements LectureApi {
    private final LectureService lectureService;

    @GetMapping("/lectures")
    public ResponseEntity<List<LectureResponse>> getLectureList(@RequestParam(name = "semester_date") String semester) {

        List<LectureResponse> lectureList = lectureService.getLecturesBySemester(semester);
        return ResponseEntity.ok(lectureList);
    }
}
