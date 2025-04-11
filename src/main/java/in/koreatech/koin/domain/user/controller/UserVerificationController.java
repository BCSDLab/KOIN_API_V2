package in.koreatech.koin.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.user.dto.verification.EmailSendVerificationCodeRequest;
import in.koreatech.koin.domain.user.dto.verification.EmailVerifyVerificationCodeRequest;
import in.koreatech.koin.domain.user.dto.verification.SmsSendVerificationCodeRequest;
import in.koreatech.koin.domain.user.dto.verification.SmsVerifyVerificationCodeRequest;
import in.koreatech.koin.domain.user.dto.verification.VerificationCountResponse;
import in.koreatech.koin.domain.user.service.UserVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/verification")
public class UserVerificationController implements UserVerificationApi {

    private final UserVerificationService userVerificationService;

    @PostMapping("/sms/send")
    public ResponseEntity<VerificationCountResponse> sendSmsVerificationCode(
        @Valid @RequestBody SmsSendVerificationCodeRequest request
    ) {
        VerificationCountResponse response = userVerificationService.sendCode(request.phoneNumber());
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/sms/verify")
    public ResponseEntity<Void> verifySmsVerificationCode(
        @Valid @RequestBody SmsVerifyVerificationCodeRequest request
    ) {
        userVerificationService.verifyCode(request.phoneNumber(), request.verificationCode());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email/send")
    public ResponseEntity<VerificationCountResponse> sendEmailVerificationCode(
        @Valid @RequestBody EmailSendVerificationCodeRequest request
    ) {
        VerificationCountResponse response = userVerificationService.sendCode(request.email());
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/email/verify")
    public ResponseEntity<Void> verifyEmailVerificationCode(
        @Valid @RequestBody EmailVerifyVerificationCodeRequest request
    ) {
        userVerificationService.verifyCode(request.email(), request.verificationCode());
        return ResponseEntity.ok().build();
    }
}
