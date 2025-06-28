package in.koreatech.koin.domain.user.verification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.user.verification.dto.SendEmailVerificationRequest;
import in.koreatech.koin.domain.user.verification.dto.SendSmsVerificationRequest;
import in.koreatech.koin.domain.user.verification.dto.SendVerificationResponse;
import in.koreatech.koin.domain.user.verification.dto.VerifyEmailVerificationRequest;
import in.koreatech.koin.domain.user.verification.dto.VerifySmsVerificationRequest;
import in.koreatech.koin.domain.user.verification.model.VerificationChannel;
import in.koreatech.koin.domain.user.verification.service.UserVerificationService;
import in.koreatech.koin.web.ipaddress.IpAddress;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/verification")
public class UserVerificationController implements UserVerificationApi {

    private final UserVerificationService userVerificationService;

    @PostMapping("/sms/send")
    public ResponseEntity<SendVerificationResponse> sendSmsVerificationCode(
        @Valid @RequestBody SendSmsVerificationRequest request, @IpAddress String ipAddress
    ) {
        SendVerificationResponse response = userVerificationService.sendVerification(request.phoneNumber(), ipAddress, VerificationChannel.SMS);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/sms/verify")
    public ResponseEntity<Void> verifySmsVerificationCode(
        @Valid @RequestBody VerifySmsVerificationRequest request, @IpAddress String ipAddress
    ) {
        userVerificationService.verifyCode(request.phoneNumber(), ipAddress, request.verificationCode());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/email/send")
    public ResponseEntity<SendVerificationResponse> sendEmailVerificationCode(
        @Valid @RequestBody SendEmailVerificationRequest request, @IpAddress String ipAddress
    ) {
        SendVerificationResponse response = userVerificationService.sendVerification(request.email(), ipAddress, VerificationChannel.EMAIL);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/email/verify")
    public ResponseEntity<Void> verifyEmailVerificationCode(
        @Valid @RequestBody VerifyEmailVerificationRequest request, @IpAddress String ipAddress
    ) {
        userVerificationService.verifyCode(request.email(), request.verificationCode(), ipAddress);
        return ResponseEntity.ok().build();
    }
}
