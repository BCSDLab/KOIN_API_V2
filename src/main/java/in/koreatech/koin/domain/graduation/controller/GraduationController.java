package in.koreatech.koin.domain.graduation.controller;

import in.koreatech.koin.domain.graduation.service.GraduationService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

@RestController
@RequiredArgsConstructor
public class GraduationController implements GraduationApi{

    private final GraduationService graduationService;

    @PostMapping("/graduation/agree")
    public ResponseEntity<Void> createStudentCourseCalculation(
            @Auth(permit = {STUDENT}) Integer userId)
    {
        graduationService.createStudentCourseCalculation(userId);
        return ResponseEntity.ok().build();
    }
}
