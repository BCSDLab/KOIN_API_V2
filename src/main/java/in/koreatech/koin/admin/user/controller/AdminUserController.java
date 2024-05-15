package in.koreatech.koin.admin.user.controller;

import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.user.dto.AdminOwnerResponse;
import in.koreatech.koin.admin.user.dto.AdminStudentResponse;
import in.koreatech.koin.admin.user.dto.AdminStudentUpdateRequest;
import in.koreatech.koin.admin.user.dto.AdminStudentUpdateResponse;
import in.koreatech.koin.admin.user.service.AdminUserService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping("/admin/users/student/{id}")
    public ResponseEntity<AdminStudentResponse> getStudent(
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminStudentResponse adminStudentResponse = adminUserService.getStudent(id);
        return ResponseEntity.ok(adminStudentResponse);
    };

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
    ResponseEntity<AdminOwnerResponse> getOwner(
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminOwnerResponse adminOwnerResponse = adminUserService.getOwner(id);
        return ResponseEntity.ok(adminOwnerResponse);
    }
}
