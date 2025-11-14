package in.koreatech.koin.admin.semester.controller;

import static in.koreatech.koin.admin.history.enums.DomainType.COOP_SEMESTER;
import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.history.aop.AdminActivityLogging;
import in.koreatech.koin.admin.semester.dto.AdminSemesterCreateRequest;
import in.koreatech.koin.admin.semester.dto.AdminSemesterResponse;
import in.koreatech.koin.admin.semester.service.AdminCoopShopSemesterService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminSemesterController implements AdminSemesterApi {

    private final AdminCoopShopSemesterService adminCoopShopSemesterService;

    @AdminActivityLogging(domain = COOP_SEMESTER)
    @PostMapping("/admin/coopshop/semesters")
    public ResponseEntity<Void> createCoopshopSemester(
        @Valid @RequestBody AdminSemesterCreateRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminCoopShopSemesterService.createCoopshopSemester(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/coopshop/semesters")
    public ResponseEntity<List<AdminSemesterResponse>> getCoopshopSemesters(
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        List<AdminSemesterResponse> response = adminCoopShopSemesterService.getCoopshopSemesters();
        return ResponseEntity.ok(response);
    }
}
