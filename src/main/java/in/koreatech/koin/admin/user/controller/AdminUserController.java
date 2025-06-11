package in.koreatech.koin.admin.user.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.admin.abtest.useragent.UserAgent;
import in.koreatech.koin.admin.abtest.useragent.UserAgentInfo;
import in.koreatech.koin.admin.user.dto.AdminLoginRequest;
import in.koreatech.koin.admin.user.dto.AdminLoginResponse;
import in.koreatech.koin.admin.user.dto.AdminPasswordChangeRequest;
import in.koreatech.koin.admin.user.dto.AdminPermissionUpdateRequest;
import in.koreatech.koin.admin.user.dto.AdminResponse;
import in.koreatech.koin.admin.user.dto.AdminTokenRefreshRequest;
import in.koreatech.koin.admin.user.dto.AdminTokenRefreshResponse;
import in.koreatech.koin.admin.user.dto.AdminUpdateRequest;
import in.koreatech.koin.admin.user.dto.AdminsCondition;
import in.koreatech.koin.admin.user.dto.AdminsResponse;
import in.koreatech.koin.admin.user.dto.CreateAdminRequest;
import in.koreatech.koin.admin.user.enums.TeamType;
import in.koreatech.koin.admin.user.enums.TrackType;
import in.koreatech.koin.admin.user.service.AdminUserService;
import in.koreatech.koin.domain.user.model.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminUserController implements AdminUserApi{

    private final AdminUserService adminUserService;

    @PostMapping("/admin")
    public ResponseEntity<Void> createAdmin(
        @RequestBody @Valid CreateAdminRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminResponse response = adminUserService.createAdmin(request, adminId);
        return ResponseEntity.created(URI.create("/" + response.id())).build();
    }

    @PutMapping("/admin/password")
    public ResponseEntity<Void> adminPasswordChange(
        @RequestBody @Valid AdminPasswordChangeRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminUserService.adminPasswordChange(request, adminId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/user/login")
    public ResponseEntity<AdminLoginResponse> adminLogin(
        @RequestBody @Valid AdminLoginRequest request,
        @UserAgent UserAgentInfo userAgentInfo
    ) {
        AdminLoginResponse response = adminUserService.adminLogin(request);
        return ResponseEntity.created(URI.create("/"))
            .body(response);
    }

    @PostMapping("admin/user/logout")
    public ResponseEntity<Void> logout(
        @Auth(permit = {ADMIN}) Integer adminId,
        @UserAgent UserAgentInfo userAgentInfo
    ) {
        adminUserService.adminLogout(adminId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/user/refresh")
    public ResponseEntity<AdminTokenRefreshResponse> refresh(
        @RequestBody @Valid AdminTokenRefreshRequest request,
        @UserAgent UserAgentInfo userAgentInfo
    ) {
        AdminTokenRefreshResponse tokenGroupResponse = adminUserService.adminRefresh(request);
        return ResponseEntity.created(URI.create("/"))
            .body(tokenGroupResponse);
    }

    @GetMapping("/admin")
    public ResponseEntity<AdminResponse> getLoginAdminInfo(
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminResponse adminResponse = adminUserService.getAdmin(adminId);
        return ResponseEntity.ok(adminResponse);
    }

    @GetMapping("/admin/{id}")
    public ResponseEntity<AdminResponse> getAdmin(
        @PathVariable("id") Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminResponse adminResponse = adminUserService.getAdmin(id);
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
        AdminsResponse adminsResponse = adminUserService.getAdmins(adminsCondition);
        return ResponseEntity.ok(adminsResponse);
    }

    @PutMapping("/admin/{id}/authed")
    public ResponseEntity<Void> adminAuthenticate(
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminUserService.adminAuthenticate(id, adminId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<Void> updateAdmin(
        @RequestBody @Valid AdminUpdateRequest request,
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminUserService.updateAdmin(request, id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/admin/{id}/permission")
    public ResponseEntity<Void> updateAdminPermission(
        @RequestBody @Valid AdminPermissionUpdateRequest request,
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminUserService.updateAdminPermission(request, id, adminId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/users/{id}")
    public ResponseEntity<User> getUser(
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminUserService.getUser(id));
    }

    @DeleteMapping("/admin/users/{id}")
    public ResponseEntity<Void> deleteUser(
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminUserService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/users/{id}/undelete")
    public ResponseEntity<Void> undeleteUser(
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminUserService.undeleteUser(id);
        return ResponseEntity.ok().build();
    }
}
