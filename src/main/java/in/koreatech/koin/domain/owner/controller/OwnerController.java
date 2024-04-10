package in.koreatech.koin.domain.owner.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.owner.dto.OwnerPasswordResetVerifyRequest;
import in.koreatech.koin.domain.owner.dto.OwnerPasswordUpdateRequest;
import in.koreatech.koin.domain.owner.dto.OwnerRegisterRequest;
import in.koreatech.koin.domain.owner.dto.OwnerResponse;
import in.koreatech.koin.domain.owner.dto.OwnerSendEmailRequest;
import in.koreatech.koin.domain.owner.dto.OwnerVerifyRequest;
import in.koreatech.koin.domain.owner.dto.OwnerVerifyResponse;
import in.koreatech.koin.domain.owner.dto.VerifyEmailRequest;
import in.koreatech.koin.domain.owner.service.OwnerService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OwnerController implements OwnerApi {

    private final OwnerService ownerService;

    @PostMapping("/owners/verification/email")
    public ResponseEntity<Void> requestVerificationToRegister(
        @RequestBody @Valid VerifyEmailRequest request
    ) {
        ownerService.requestSignUpEmailVerification(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/owner")
    public ResponseEntity<OwnerResponse> getOwner(
        @Auth(permit = {OWNER}) Integer ownerId
    ) {
        OwnerResponse ownerInfo = ownerService.getOwner(ownerId);
        return ResponseEntity.ok().body(ownerInfo);
    }

    @PostMapping("/owners/register")
    public ResponseEntity<Void> register(
        @Valid @RequestBody OwnerRegisterRequest request
    ) {
        ownerService.register(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/owners/verification/code")
    public ResponseEntity<OwnerVerifyResponse> codeVerification(
        @Valid @RequestBody OwnerVerifyRequest request
    ) {
        OwnerVerifyResponse response = ownerService.verifyCode(request);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/owners/password/reset/verification")
    public ResponseEntity<Void> sendResetPasswordEmail(
        @Valid @RequestBody OwnerSendEmailRequest request
    ) {
        ownerService.sendResetPasswordEmail(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/owners/password/reset/send")
    public ResponseEntity<Void> sendVerifyCode(
        @Valid @RequestBody OwnerPasswordResetVerifyRequest request
    ) {
        ownerService.verifyResetPasswordCode(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/owners/password/reset")
    public ResponseEntity<Void> updatePassword(
        @Valid @RequestBody OwnerPasswordUpdateRequest request
    ) {
        ownerService.updatePassword(request);
        return ResponseEntity.ok().build();
    }
}
