package in.koreatech.koin.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.user.dto.verification.SendEmailVerificationRequest;
import in.koreatech.koin.domain.user.dto.verification.VerifyEmailVerificationRequest;
import in.koreatech.koin.domain.user.dto.verification.SendSmsVerificationRequest;
import in.koreatech.koin.domain.user.dto.verification.VerifySmsVerificationRequest;
import in.koreatech.koin.domain.user.dto.verification.SendVerificationResponse;
import in.koreatech.koin.domain.user.service.UserVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/verification")
public class UserVerificationController implements UserVerificationApi {

    private final UserVerificationService userVerificationService;

    @PostMapping("/sms/send")
    public ResponseEntity<SendVerificationResponse> sendSmsVerificationCode(
        @Valid @RequestBody SendSmsVerificationRequest request
    ) {
        SendVerificationResponse response = userVerificationService.sendCode(request.phoneNumber());
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
        SendVerificationResponse response = userVerificationService.sendCode(request.email());
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
