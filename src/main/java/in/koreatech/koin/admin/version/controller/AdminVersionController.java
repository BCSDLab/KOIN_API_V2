package in.koreatech.koin.admin.version.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.version.dto.AdminVersionHistoryResponse;
import in.koreatech.koin.admin.version.dto.AdminVersionUpdateRequest;
import in.koreatech.koin.admin.version.dto.AdminVersionResponse;
import in.koreatech.koin.admin.version.dto.AdminVersionsResponse;
import in.koreatech.koin.admin.version.service.AdminVersionService;
import in.koreatech.koin._common.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/version")
public class AdminVersionController implements AdminVersionApi {

    private final AdminVersionService adminVersionService;

    @GetMapping
    public ResponseEntity<AdminVersionsResponse> getVersions(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminVersionService.getVersions(page, limit));
    }

    @GetMapping("/{type}")
    public ResponseEntity<AdminVersionResponse> getVersion(
        @PathVariable("type") String type,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminVersionService.getVersion(type));
    }

    @PostMapping("/{type}")
    public ResponseEntity<Void> updateVersion(
        @PathVariable("type") String type,
        @RequestBody @Valid AdminVersionUpdateRequest adminVersionUpdateRequest,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminVersionService.updateVersion(type, adminVersionUpdateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/history/{type}")
    public ResponseEntity<AdminVersionHistoryResponse> getHistory(
        @RequestParam(name = "page", defaultValue = "1") Integer page,
        @RequestParam(name = "limit", defaultValue = "10", required = false) Integer limit,
        @PathVariable("type") String type,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminVersionService.getHistory(type, page, limit));
    }
}
