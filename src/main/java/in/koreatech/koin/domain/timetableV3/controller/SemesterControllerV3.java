package in.koreatech.koin.domain.timetableV3.controller;

import static in.koreatech.koin.domain.user.model.UserType.COUNCIL;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.timetableV3.dto.response.SemesterCheckResponseV3;
import in.koreatech.koin.domain.timetableV3.dto.response.SemesterResponseV3;
import in.koreatech.koin.domain.timetableV3.service.SemesterServiceV3;
import in.koreatech.koin._common.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SemesterControllerV3 implements SemesterApiV3 {

    private final SemesterServiceV3 semesterServiceV3;

    @GetMapping("/v3/semesters")
    public ResponseEntity<List<SemesterResponseV3>> getSemesters() {
        List<SemesterResponseV3> semesterResponseV3 = semesterServiceV3.getSemesters();
        return ResponseEntity.ok(semesterResponseV3);
    }

    @GetMapping("/v3/semesters/check")
    public ResponseEntity<SemesterCheckResponseV3> getStudentSemesters(
        @Auth(permit = {STUDENT, COUNCIL}) Integer userId
    ) {
        SemesterCheckResponseV3 semesterCheckResponseV3 = semesterServiceV3.getStudentSemesters(userId);
        return ResponseEntity.ok(semesterCheckResponseV3);
    }
}
