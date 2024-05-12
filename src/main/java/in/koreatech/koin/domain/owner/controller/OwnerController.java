package in.koreatech.koin.domain.owner.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.owner.dto.OwnerEmailVerifyRequest;
import in.koreatech.koin.domain.owner.dto.OwnerPasswordResetVerifyEmailRequest;
import in.koreatech.koin.domain.owner.dto.OwnerPasswordResetVerifyPhoneRequest;
import in.koreatech.koin.domain.owner.dto.OwnerPasswordUpdateEmailRequest;
import in.koreatech.koin.domain.owner.dto.OwnerPasswordUpdatePhoneRequest;
import in.koreatech.koin.domain.owner.dto.OwnerPhoneVerifyRequest;
import in.koreatech.koin.domain.owner.dto.OwnerRegisterRequest;
import in.koreatech.koin.domain.owner.dto.OwnerResponse;
import in.koreatech.koin.domain.owner.dto.OwnerSendEmailRequest;
import in.koreatech.koin.domain.owner.dto.OwnerSendPhoneRequest;
import in.koreatech.koin.domain.owner.dto.OwnerVerifyResponse;
import in.koreatech.koin.domain.owner.dto.VerifyEmailRequest;
import in.koreatech.koin.domain.owner.dto.VerifyPhoneRequest;
import in.koreatech.koin.domain.owner.service.OwnerService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OwnerController implements OwnerApi {

    private final OwnerService ownerService;

    @PostMapping("/owners/verification/email")
    public ResponseEntity<Void> requestVerificationToRegisterByEmail(
        @RequestBody @Valid VerifyEmailRequest request
    ) {
        ownerService.requestSignUpEmailVerification(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/owners/verification/phone")
    public ResponseEntity<Void> requestVerificationToRegisterByPhone(
        @RequestBody @Valid VerifyPhoneRequest verifyPhoneRequest
    ) {
        ownerService.requestSignUpPhoneVerification(verifyPhoneRequest);
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
        @Valid @RequestBody OwnerEmailVerifyRequest request
    ) {
        OwnerVerifyResponse response = ownerService.verifyCodeByEmail(request);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/owners/verification/code/phone")
    public ResponseEntity<OwnerVerifyResponse> codeVerification(
        @Valid @RequestBody OwnerPhoneVerifyRequest request
    ) {
        OwnerVerifyResponse response = ownerService.verifyCodeByPhone(request);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/owners/password/reset/verification")
    public ResponseEntity<Void> sendResetPasswordByEmail(
        @Valid @RequestBody OwnerSendEmailRequest request
    ) {
        ownerService.sendResetPasswordByEmail(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/owners/password/reset/verification/phone")
    public ResponseEntity<Void> sendResetPasswordByPhone(
        @Valid @RequestBody OwnerSendPhoneRequest request
    ) {
        ownerService.sendResetPasswordByPhone(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/owners/password/reset/send")
    public ResponseEntity<Void> sendVerifyCode(
        @Valid @RequestBody OwnerPasswordResetVerifyEmailRequest request
    ) {
        ownerService.verifyResetPasswordCodeByEmail(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/owners/password/reset/send/phone")
    public ResponseEntity<Void> sendVerifyCodeByPhone(
        @Valid @RequestBody OwnerPasswordResetVerifyPhoneRequest request
    ) {
        ownerService.verifyResetPasswordCodeByPhone(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/owners/password/reset")
    public ResponseEntity<Void> updatePasswordByEmail(
        @Valid @RequestBody OwnerPasswordUpdateEmailRequest request
    ) {
        ownerService.updatePasswordByEmail(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/owners/password/reset/phone")
    public ResponseEntity<Void> updatePasswordByPhone(
        @Valid @RequestBody OwnerPasswordUpdatePhoneRequest request
    ) {
        ownerService.updatePasswordByPhone(request);
        return ResponseEntity.ok().build();
    }
}
