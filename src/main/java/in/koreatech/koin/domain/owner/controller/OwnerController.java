package in.koreatech.koin.domain.owner.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.owner.dto.CompanyNumberCheckRequest;
import in.koreatech.koin.domain.owner.dto.OwnerRegisteredInfoResponse;
import in.koreatech.koin.domain.owner.dto.OwnerResponse;
import in.koreatech.koin.domain.owner.service.OwnerService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OwnerController implements OwnerApi {

    private final OwnerService ownerService;

    @GetMapping("/owner")
    public ResponseEntity<OwnerResponse> getOwner(
        @Auth(permit = {OWNER}) Integer ownerId
    ) {
        OwnerResponse ownerInfo = ownerService.getOwner(ownerId);
        return ResponseEntity.ok().body(ownerInfo);
    }

    @PostMapping("/owners/exists/company-number")
    public ResponseEntity<Void> checkCompanyNumber(
        @Valid @RequestBody CompanyNumberCheckRequest request
    ) {
        ownerService.checkCompanyNumber(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/owner/registered-store")
    public ResponseEntity<OwnerRegisteredInfoResponse> getOwnerRegisteredStoreInfo(
        @Auth(permit = {OWNER}) Integer userId
    ) {
        OwnerRegisteredInfoResponse response = ownerService.getOwnerRegisteredStoreInfo(userId);
        return ResponseEntity.ok().body(response);
    }
}
