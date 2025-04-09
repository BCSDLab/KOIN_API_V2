package in.koreatech.koin.domain.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.user.dto.verification.EmailFindIdRequest;
import in.koreatech.koin.domain.user.dto.verification.EmailResetPasswordRequest;
import in.koreatech.koin.domain.user.dto.verification.EmailSendVerificationCodeRequest;
import in.koreatech.koin.domain.user.dto.verification.EmailVerifyVerificationCodeRequest;
import in.koreatech.koin.domain.user.dto.verification.FindIdResponse;
import in.koreatech.koin.domain.user.dto.verification.SmsFindIdRequest;
import in.koreatech.koin.domain.user.dto.verification.SmsResetPasswordRequest;
import in.koreatech.koin.domain.user.dto.verification.SmsSendVerificationCodeRequest;
import in.koreatech.koin.domain.user.dto.verification.SmsVerifyVerificationCodeRequest;
import in.koreatech.koin.domain.user.dto.verification.VerificationCountResponse;
import in.koreatech.koin.domain.user.service.UserVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserVerificationController implements UserVerificationApi {

    private final UserVerificationService userVerificationService;

    @PostMapping("/user/sms/send-code")
    public ResponseEntity<VerificationCountResponse> sendSmsVerificationCode(
        @Valid @RequestBody SmsSendVerificationCodeRequest request
    ) {
        VerificationCountResponse response = userVerificationService.sendCode(request.phoneNumber());
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/user/sms/verify-code")
    public ResponseEntity<Void> verifySmsVerificationCode(
        @Valid @RequestBody SmsVerifyVerificationCodeRequest request
    ) {
        userVerificationService.verifyCode(request.phoneNumber(), request.verificationCode());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/sms/find-id")
    public ResponseEntity<FindIdResponse> findIdBySmsVerification(
        @Valid @RequestBody SmsFindIdRequest request
    ) {
        String userId = userVerificationService.findIdByVerification(request.phoneNumber());
        return ResponseEntity.ok().body(FindIdResponse.from(userId));
    }

    @PostMapping("/user/sms/reset-password")
    public ResponseEntity<Void> resetPasswordBySmsVerification(
        @Valid @RequestBody SmsResetPasswordRequest request
    ) {
        userVerificationService.resetPasswordByVerification(request.userId(), request.phoneNumber(),
            request.newPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/email/send-code")
    public ResponseEntity<VerificationCountResponse> sendEmailVerificationCode(
        @Valid @RequestBody EmailSendVerificationCodeRequest request
    ) {
        VerificationCountResponse response = userVerificationService.sendCode(request.email());
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/user/email/verify-code")
    public ResponseEntity<Void> verifyEmailVerificationCode(
        @Valid @RequestBody EmailVerifyVerificationCodeRequest request
    ) {
        userVerificationService.verifyCode(request.email(), request.verificationCode());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/email/find-id")
    public ResponseEntity<FindIdResponse> findIdByEmailVerification(
        @Valid @RequestBody EmailFindIdRequest request
    ) {
        String userId = userVerificationService.findIdByVerification(request.email());
        return ResponseEntity.ok().body(FindIdResponse.from(userId));
    }

    @PostMapping("/user/email/reset-password")
    public ResponseEntity<Void> resetPasswordByEmailVerification(
        @Valid @RequestBody EmailResetPasswordRequest request
    ) {
        userVerificationService.resetPasswordByVerification(null, request.email(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}
