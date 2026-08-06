package in.koreatech.koin.admin.lecture.controller;

import static in.koreatech.koin.admin.history.enums.DomainType.LECTURES;
import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.history.aop.AdminActivityLogging;
import in.koreatech.koin.admin.lecture.dto.AdminLectureCreateRequest;
import in.koreatech.koin.admin.lecture.service.AdminLectureService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminLectureController implements AdminLectureApi {

    private final AdminLectureService adminLectureService;

    @PostMapping("/admin/lectures")
    @AdminActivityLogging(domain = LECTURES)
    public ResponseEntity<Void> createLectures(
        @RequestBody @Valid AdminLectureCreateRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminLectureService.createLectures(request);
        return ResponseEntity.ok().build();
    }
}
