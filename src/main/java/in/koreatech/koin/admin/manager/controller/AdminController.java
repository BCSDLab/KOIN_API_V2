package in.koreatech.koin.admin.manager.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.abtest.useragent.UserAgent;
import in.koreatech.koin.admin.abtest.useragent.UserAgentInfo;
import in.koreatech.koin.admin.history.aop.AdminActivityLogging;
import in.koreatech.koin.admin.history.enums.DomainType;
import in.koreatech.koin.admin.manager.dto.request.AdminLoginRequest;
import in.koreatech.koin.admin.manager.dto.request.AdminPasswordChangeRequest;
import in.koreatech.koin.admin.manager.dto.request.AdminPermissionUpdateRequest;
import in.koreatech.koin.admin.manager.dto.request.AdminTokenRefreshRequest;
import in.koreatech.koin.admin.manager.dto.request.AdminUpdateRequest;
import in.koreatech.koin.admin.manager.dto.request.AdminsCondition;
import in.koreatech.koin.admin.manager.dto.response.AdminLoginResponse;
import in.koreatech.koin.admin.manager.dto.response.AdminResponse;
import in.koreatech.koin.admin.manager.dto.response.AdminTokenRefreshResponse;
import in.koreatech.koin.admin.manager.dto.response.AdminsResponse;
import in.koreatech.koin.admin.manager.dto.response.CreateAdminRequest;
import in.koreatech.koin.admin.manager.enums.TeamType;
import in.koreatech.koin.admin.manager.enums.TrackType;
import in.koreatech.koin.admin.manager.service.AdminService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminController implements AdminApi {

    private final AdminService adminService;

    @PostMapping("/admin")
    public ResponseEntity<Void> createAdmin(
        @RequestBody @Valid CreateAdminRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminResponse response = adminService.createAdmin(request, adminId);
        return ResponseEntity.created(URI.create("/" + response.id())).build();
    }

    @PutMapping("/admin/password")
    public ResponseEntity<Void> adminPasswordChange(
        @RequestBody @Valid AdminPasswordChangeRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminService.adminPasswordChange(request, adminId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/user/login")
    public ResponseEntity<AdminLoginResponse> adminLogin(
        @RequestBody @Valid AdminLoginRequest request,
        @UserAgent UserAgentInfo userAgentInfo
    ) {
        AdminLoginResponse response = adminService.adminLogin(request, userAgentInfo);
        return ResponseEntity.created(URI.create("/"))
            .body(response);
    }

    @PostMapping("admin/user/logout")
    public ResponseEntity<Void> logout(
        @Auth(permit = {ADMIN}) Integer adminId,
        @UserAgent UserAgentInfo userAgentInfo
    ) {
        adminService.adminLogout(adminId, userAgentInfo);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/user/refresh")
    public ResponseEntity<AdminTokenRefreshResponse> refresh(
        @RequestBody @Valid AdminTokenRefreshRequest request,
        @UserAgent UserAgentInfo userAgentInfo
    ) {
        AdminTokenRefreshResponse tokenGroupResponse = adminService.adminRefresh(request, userAgentInfo);
        return ResponseEntity.created(URI.create("/"))
            .body(tokenGroupResponse);
    }

    @GetMapping("/admin")
    public ResponseEntity<AdminResponse> getLoginAdminInfo(
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminResponse adminResponse = adminService.getAdmin(adminId);
        return ResponseEntity.ok(adminResponse);
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<AdminResponse> getAdmin(
        @PathVariable("id") Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminResponse adminResponse = adminService.getAdmin(id);
        return ResponseEntity.ok(adminResponse);
    }

    @GetMapping("/admins")
    public ResponseEntity<AdminsResponse> getAdmins(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) Boolean isAuthed,
        @RequestParam(required = false) TrackType trackName,
        @RequestParam(required = false) TeamType teamName,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminsCondition adminsCondition = new AdminsCondition(page, limit, isAuthed, trackName, teamName);
        AdminsResponse adminsResponse = adminService.getAdmins(adminsCondition);
        return ResponseEntity.ok(adminsResponse);
    }

    @PutMapping("/admin/{id}/authed")
    @AdminActivityLogging(domain = DomainType.ADMIN, domainIdParam = "id")
    public ResponseEntity<Void> adminAuthenticate(
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminService.adminAuthenticate(id, adminId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/{id}")
    @AdminActivityLogging(domain = DomainType.ADMIN, domainIdParam = "id")
    public ResponseEntity<Void> updateAdmin(
        @RequestBody @Valid AdminUpdateRequest request,
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminService.updateAdmin(request, id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/{id}/permission")
    @AdminActivityLogging(domain = DomainType.ADMIN, domainIdParam = "id")
    public ResponseEntity<Void> updateAdminPermission(
        @RequestBody @Valid AdminPermissionUpdateRequest request,
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminService.updateAdminPermission(request, id, adminId);
        return ResponseEntity.ok().build();
    }
}
