package in.koreatech.koin.admin.user.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.user.dto.AdminNewOwnersResponse;
import in.koreatech.koin.admin.user.dto.AdminOwnerResponse;
import in.koreatech.koin.admin.user.dto.AdminOwnerUpdateRequest;
import in.koreatech.koin.admin.user.dto.AdminOwnerUpdateResponse;
import in.koreatech.koin.admin.user.dto.AdminOwnersResponse;
import in.koreatech.koin.admin.user.dto.AdminStudentResponse;
import in.koreatech.koin.admin.user.dto.AdminStudentUpdateRequest;
import in.koreatech.koin.admin.user.dto.AdminStudentUpdateResponse;
import in.koreatech.koin.admin.user.dto.OwnersCondition;
import in.koreatech.koin.admin.user.service.AdminUserService;
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

    @GetMapping("/admin/users/student/{id}")
    public ResponseEntity<AdminStudentResponse> getStudent(
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminStudentResponse adminStudentResponse = adminUserService.getStudent(id);
        return ResponseEntity.ok(adminStudentResponse);
    }

    @PutMapping("/admin/users/student/{id}")
    public ResponseEntity<AdminStudentUpdateResponse> updateStudent(
        @Valid @RequestBody AdminStudentUpdateRequest adminRequest,
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminStudentUpdateResponse adminStudentUpdateResponse = adminUserService.updateStudent(id, adminRequest);
        return ResponseEntity.ok(adminStudentUpdateResponse);
    }

    @GetMapping("/admin/users/owner/{id}")
    public ResponseEntity<AdminOwnerResponse> getOwner(
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminOwnerResponse adminOwnerResponse = adminUserService.getOwner(id);
        return ResponseEntity.ok(adminOwnerResponse);
    }

    @PutMapping("/admin/users/owner/{id}")
    public ResponseEntity<AdminOwnerUpdateResponse> updateOwner(
        @PathVariable Integer id,
        @RequestBody @Valid AdminOwnerUpdateRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminOwnerUpdateResponse adminOwnerUpdateResponse = adminUserService.updateOwner(id, request);
        return ResponseEntity.ok(adminOwnerUpdateResponse);
    }

    @GetMapping("/admin/users/new-owners")
    public ResponseEntity<AdminNewOwnersResponse> getNewOwners(
        @ModelAttribute OwnersCondition ownersCondition,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminUserService.getNewOwners(ownersCondition));
    }

    @GetMapping("/admin/users/owners")
    public ResponseEntity<AdminOwnersResponse> getOwners(
        @ModelAttribute OwnersCondition ownersCondition,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminUserService.getOwners(ownersCondition));
    }
}
