package in.koreatech.koin.admin.version.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import in.koreatech.koin.admin.version.dto.AdminVersionResponse;
import in.koreatech.koin.admin.version.service.AdminVersionService;
import in.koreatech.koin.global.auth.Auth;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AdminVersionController implements AdminVersionApi {

    private final AdminVersionService adminVersionService;

    @GetMapping("/admin/version")
    public ResponseEntity<AdminVersionResponse> getVersion(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminVersionService.getVersion(page, limit));
    }
}
