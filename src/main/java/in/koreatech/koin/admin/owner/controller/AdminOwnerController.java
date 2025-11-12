package in.koreatech.koin.admin.owner.controller;

import static in.koreatech.koin.admin.history.enums.DomainType.OWNER;
import static in.koreatech.koin.domain.user.model.UserType.ADMIN;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.history.aop.AdminActivityLogging;
import in.koreatech.koin.admin.owner.dto.AdminNewOwnersResponse;
import in.koreatech.koin.admin.owner.dto.AdminOwnerResponse;
import in.koreatech.koin.admin.owner.dto.AdminOwnerUpdateRequest;
import in.koreatech.koin.admin.owner.dto.AdminOwnerUpdateResponse;
import in.koreatech.koin.admin.owner.dto.AdminOwnersResponse;
import in.koreatech.koin.admin.owner.dto.OwnersCondition;
import in.koreatech.koin.admin.owner.service.AdminOwnerService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminOwnerController implements AdminOwnerApi {

    private final AdminOwnerService adminOwnerService;

    @PutMapping("/admin/owner/{id}/authed")
    @AdminActivityLogging(domain = OWNER, domainIdParam = "id")
    public ResponseEntity<Void> allowOwnerPermission(
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        adminOwnerService.allowOwnerPermission(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/users/owner/{id}")
    public ResponseEntity<AdminOwnerResponse> getOwner(
        @PathVariable Integer id,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminOwnerResponse response = adminOwnerService.getOwner(id);
        return ResponseEntity.ok().body(response);
    }

    @PutMapping("/admin/users/owner/{id}")
    public ResponseEntity<AdminOwnerUpdateResponse> updateOwner(
        @PathVariable Integer id,
        @RequestBody @Valid AdminOwnerUpdateRequest request,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        AdminOwnerUpdateResponse response = adminOwnerService.updateOwner(id, request);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/admin/users/new-owners")
    public ResponseEntity<AdminNewOwnersResponse> getNewOwners(
        @ParameterObject @ModelAttribute OwnersCondition ownersCondition,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminOwnerService.getNewOwners(ownersCondition));
    }

    @GetMapping("/admin/users/owners")
    public ResponseEntity<AdminOwnersResponse> getOwners(
        @ParameterObject @ModelAttribute OwnersCondition ownersCondition,
        @Auth(permit = {ADMIN}) Integer adminId
    ) {
        return ResponseEntity.ok().body(adminOwnerService.getOwners(ownersCondition));
    }
}
