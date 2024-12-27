package in.koreatech.koin.domain.graduation.controller;

import in.koreatech.koin.domain.graduation.dto.GraduationCourseCalculationResponse;
import in.koreatech.koin.domain.graduation.service.GraduationService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

import org.checkerframework.checker.units.qual.A;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

@RestController
@RequiredArgsConstructor
public class GraduationController implements GraduationApi {

    private final GraduationService graduationService;

    @PostMapping("/graduation/agree")
    public ResponseEntity<Void> createStudentCourseCalculation(
        @Auth(permit = {STUDENT}) Integer userId) {
        graduationService.createStudentCourseCalculation(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/graduation/course/calculation")
    public ResponseEntity<GraduationCourseCalculationResponse> getGraduationCourseCalculation(
        @Auth(permit = {STUDENT}) Integer userId) {
        GraduationCourseCalculationResponse response = graduationService.getGraduationCourseCalculationResponse(userId);
        return ResponseEntity.ok(response);
    }
}
