package in.koreatech.koin.admin.semester.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.semester.dto.AdminSemesterCreateRequest;
import in.koreatech.koin.admin.semester.service.AdminCoopShopSemesterService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminSemesterController implements AdminSemesterApi {

    private final AdminCoopShopSemesterService adminCoopShopSemesterService;

    @PostMapping("/admin/coopshop/semesters")
    public ResponseEntity<Void> createCoopshopSemester(
        @Valid @RequestBody AdminSemesterCreateRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminCoopShopSemesterService.createCoopshopSemester(request);
        return ResponseEntity.ok().build();
    }
}
