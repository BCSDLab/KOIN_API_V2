package in.koreatech.koin.domain.user.verification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.user.verification.service.UserVerificationService;
import in.koreatech.koin.domain.user.verification.dto.SendEmailVerificationRequest;
import in.koreatech.koin.domain.user.verification.dto.SendSmsVerificationRequest;
import in.koreatech.koin.domain.user.verification.dto.SendVerificationResponse;
import in.koreatech.koin.domain.user.verification.dto.VerifyEmailVerificationRequest;
import in.koreatech.koin.domain.user.verification.dto.VerifySmsVerificationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/verification")
public class UserVerificationController implements UserVerificationApi {

    private final UserVerificationService userVerificationService;

    @PostMapping("/sms/send")
    public ResponseEntity<SendVerificationResponse> sendSmsVerificationCode(
        @Valid @RequestBody SendSmsVerificationRequest request
    ) {
        SendVerificationResponse response = userVerificationService.sendSmsVerification(request.phoneNumber());
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/sms/verify")
    public ResponseEntity<Void> verifySmsVerificationCode(
        @Valid @RequestBody VerifySmsVerificationRequest request
    ) {
        userVerificationService.verifyCode(request.phoneNumber(), request.verificationCode());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email/send")
    public ResponseEntity<SendVerificationResponse> sendEmailVerificationCode(
        @Valid @RequestBody SendEmailVerificationRequest request
    ) {
        SendVerificationResponse response = userVerificationService.sendEmailVerification(request.email());
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/email/verify")
    public ResponseEntity<Void> verifyEmailVerificationCode(
        @Valid @RequestBody VerifyEmailVerificationRequest request
    ) {
        userVerificationService.verifyCode(request.email(), request.verificationCode());
        return ResponseEntity.ok().build();
    }
}
