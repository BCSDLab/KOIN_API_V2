package in.koreatech.koin.domain.graduation.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.AbstractController;

import in.koreatech.koin.domain.graduation.service.GraduationService;
import in.koreatech.koin.domain.user.model.UserType;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class GraduationController implements GraduationApi {

    private final GraduationService graduationService;

    @PostMapping("/graduation/excel/upload")
    public ResponseEntity<String> uploadStudentGradeExcelFile(
        @RequestParam(value = "file", required = false) MultipartFile file,
        @Auth(permit = {UserType.STUDENT}) Integer userId
    ) {
        try {
            graduationService.readStudentGradeExcelFile(file, userId);
            return ResponseEntity.ok("파일이 성공적으로 업로드되었습니다.");
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
