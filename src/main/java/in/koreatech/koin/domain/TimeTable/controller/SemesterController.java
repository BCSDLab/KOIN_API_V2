package in.koreatech.koin.domain.TimeTable.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.TimeTable.dto.SemesterResponse;
import in.koreatech.koin.domain.TimeTable.service.SemesterService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SemesterController {

    private final SemesterService semesterService;

    @GetMapping("/semesters")
    public ResponseEntity<List<SemesterResponse>> getSemesters()
    {
        List<SemesterResponse> semesterResponse = semesterService.getSemesters();
        return ResponseEntity.ok(semesterResponse);
    }
}
