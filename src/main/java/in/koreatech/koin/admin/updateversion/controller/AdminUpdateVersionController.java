package in.koreatech.koin.admin.updateversion.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.updateversion.dto.AdminUpdateHistoryResponse;
import in.koreatech.koin.admin.updateversion.dto.AdminUpdateVersionRequest;
import in.koreatech.koin.admin.updateversion.dto.AdminUpdateVersionResponse;
import in.koreatech.koin.admin.updateversion.dto.AdminUpdateVersionsResponse;
import in.koreatech.koin.admin.updateversion.service.AdminUpdateVersionService;
import in.koreatech.koin.domain.updateversion.model.UpdateVersionType;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/update")
public class AdminUpdateVersionController implements AdminUpdateVersionApi {

    private final AdminUpdateVersionService adminUpdateVersionService;

    @GetMapping("/version")
    public ResponseEntity<AdminUpdateVersionsResponse> getVersions(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminUpdateVersionService.getVersions(page, limit));
    }

    @GetMapping("/version/{type}")
    public ResponseEntity<AdminUpdateVersionResponse> getVersion(
        @PathVariable("type") UpdateVersionType type,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminUpdateVersionService.getVersion(type));
    }

    @PostMapping("/version/{type}")
    public ResponseEntity<Void> updateVersion(
        @PathVariable("type") UpdateVersionType type,
        @RequestBody @Valid AdminUpdateVersionRequest adminUpdateVersionRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminUpdateVersionService.updateVersion(type, adminUpdateVersionRequest);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/version/history/{type}")
    public ResponseEntity<AdminUpdateHistoryResponse> getHistory(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @PathVariable("type") UpdateVersionType type,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminUpdateVersionService.getUpdateHistory(type, page, limit));
    }
}
