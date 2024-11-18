package in.koreatech.koin.domain.owner.controller;

import in.koreatech.koin.domain.owner.dto.OwnerEmailVerifyRequest;
import in.koreatech.koin.domain.owner.dto.OwnerPasswordResetVerifyEmailRequest;
import in.koreatech.koin.domain.owner.dto.OwnerPasswordUpdateEmailRequest;
import in.koreatech.koin.domain.owner.dto.OwnerRegisterRequest;
import in.koreatech.koin.domain.owner.dto.OwnerSendEmailRequest;
import in.koreatech.koin.domain.owner.dto.OwnerVerifyResponse;
import in.koreatech.koin.domain.owner.dto.VerifyEmailRequest;
import in.koreatech.koin.domain.owner.service.OwnerEmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmailBasedOwnerController implements EmailBasedOwnerApi {

    private final OwnerEmailVerificationService ownerEmailService;

    @PostMapping("/owners/register")
    public ResponseEntity<Void> register(
            @Valid @RequestBody OwnerRegisterRequest request
    ) {
        ownerEmailService.register(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/owners/verification/email")
    public ResponseEntity<Void> requestVerificationToRegisterByEmail(
        @RequestBody @Valid VerifyEmailRequest request
    ) {
        ownerEmailService.requestSignUpEmailVerification(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/owners/verification/code")
    public ResponseEntity<OwnerVerifyResponse> codeVerification(
        @Valid @RequestBody OwnerEmailVerifyRequest request
    ) {
        OwnerVerifyResponse response = ownerEmailService.verifyCodeByEmail(request);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/owners/password/reset/verification")
    public ResponseEntity<Void> sendResetPasswordByEmail(
        @Valid @RequestBody OwnerSendEmailRequest request
    ) {
        ownerEmailService.sendResetPasswordByEmail(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/owners/password/reset/send")
    public ResponseEntity<Void> sendVerifyCode(
        @Valid @RequestBody OwnerPasswordResetVerifyEmailRequest request
    ) {
        ownerEmailService.verifyResetPasswordCodeByEmail(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/owners/password/reset")
    public ResponseEntity<Void> updatePasswordByEmail(
        @Valid @RequestBody OwnerPasswordUpdateEmailRequest request
    ) {
        ownerEmailService.updatePasswordByEmail(request);
        return ResponseEntity.ok().build();
    }
}
