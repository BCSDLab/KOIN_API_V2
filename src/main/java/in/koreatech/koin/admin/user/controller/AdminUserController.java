package in.koreatech.koin.admin.user.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springdoc.core.annotations.ParameterObject;
import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.user.dto.AdminLoginRequest;
import in.koreatech.koin.admin.user.dto.AdminLoginResponse;
import in.koreatech.koin.admin.user.dto.AdminNewOwnersResponse;
import in.koreatech.koin.admin.user.dto.AdminOwnerResponse;
import in.koreatech.koin.admin.user.dto.AdminOwnerUpdateRequest;
import in.koreatech.koin.admin.user.dto.AdminOwnerUpdateResponse;
import in.koreatech.koin.admin.user.dto.AdminOwnersResponse;
import in.koreatech.koin.admin.user.dto.AdminStudentResponse;
import in.koreatech.koin.admin.user.dto.AdminStudentUpdateRequest;
import in.koreatech.koin.admin.user.dto.AdminStudentUpdateResponse;
import in.koreatech.koin.admin.user.dto.OwnersCondition;
import in.koreatech.koin.admin.user.dto.AdminStudentsResponse;
import in.koreatech.koin.admin.user.dto.AdminTokenRefreshRequest;
import in.koreatech.koin.admin.user.dto.AdminTokenRefreshResponse;
import in.koreatech.koin.admin.user.dto.StudentsCondition;
import in.koreatech.koin.admin.user.service.AdminUserService;
import in.koreatech.koin.domain.user.model.User;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminUserController implements AdminUserApi{

    private final AdminUserService adminUserService;

    @PutMapping("/admin/owner/{id}/authed")
    public ResponseEntity<Void> allowOwnerPermission(
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId) {
        adminUserService.allowOwnerPermission(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/students")
    public ResponseEntity<AdminStudentsResponse> getStudents(
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) Boolean isAuthed,
        @RequestParam(required = false) String nickname,
        @RequestParam(required = false) String email,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        StudentsCondition studentsCondition = new StudentsCondition(page, limit, isAuthed, nickname, email);
        AdminStudentsResponse adminStudentsResponse = adminUserService.getStudents(studentsCondition);
        return ResponseEntity.ok().body(adminStudentsResponse);
    }

    @PostMapping("/admin/user/login")
    public ResponseEntity<AdminLoginResponse> adminLogin(
        @RequestBody @Valid AdminLoginRequest request
    ) {
        AdminLoginResponse response = adminUserService.adminLogin(request);
        return ResponseEntity.created(URI.create("/"))
            .body(response);
    }

    @PostMapping("admin/user/logout")
    public ResponseEntity<Void> logout(
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminUserService.adminLogout(adminId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/admin/user/refresh")
    public ResponseEntity<AdminTokenRefreshResponse> refresh(
        @RequestBody @Valid AdminTokenRefreshRequest request
    ) {
        AdminTokenRefreshResponse tokenGroupResponse = adminUserService.adminRefresh(request);
        return ResponseEntity.created(URI.create("/"))
            .body(tokenGroupResponse);
    }


    @GetMapping("/admin/users/student/{id}")
    public ResponseEntity<AdminStudentResponse> getStudent(
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminStudentResponse adminStudentResponse = adminUserService.getStudent(id);
        return ResponseEntity.ok().body(adminStudentResponse);
    }

    @PutMapping("/admin/users/student/{id}")
    public ResponseEntity<AdminStudentUpdateResponse> updateStudent(
        @Valid @RequestBody AdminStudentUpdateRequest adminRequest,
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminStudentUpdateResponse adminStudentUpdateResponse = adminUserService.updateStudent(id, adminRequest);
        return ResponseEntity.ok().body(adminStudentUpdateResponse);
    }

    @GetMapping("/admin/users/owner/{id}")
    public ResponseEntity<AdminOwnerResponse> getOwner(
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminOwnerResponse adminOwnerResponse = adminUserService.getOwner(id);
        return ResponseEntity.ok().body(adminOwnerResponse);
    }

    @PutMapping("/admin/users/owner/{id}")
    public ResponseEntity<AdminOwnerUpdateResponse> updateOwner(
        @PathVariable Integer id,
        @RequestBody @Valid AdminOwnerUpdateRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminOwnerUpdateResponse adminOwnerUpdateResponse = adminUserService.updateOwner(id, request);
        return ResponseEntity.ok().body(adminOwnerUpdateResponse);
    }

    @GetMapping("/admin/users/new-owners")
    public ResponseEntity<AdminNewOwnersResponse> getNewOwners(
        @ParameterObject @ModelAttribute OwnersCondition ownersCondition,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminUserService.getNewOwners(ownersCondition));
    }

    @GetMapping("/admin/users/owners")
    public ResponseEntity<AdminOwnersResponse> getOwners(
        @ParameterObject @ModelAttribute OwnersCondition ownersCondition,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminUserService.getOwners(ownersCondition));
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
