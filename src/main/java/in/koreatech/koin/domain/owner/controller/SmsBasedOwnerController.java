package in.koreatech.koin.domain.owner.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.admin.abtest.useragent.UserAgent;
import in.koreatech.koin.admin.abtest.useragent.UserAgentInfo;
import in.koreatech.koin.domain.owner.dto.OwnerVerifyResponse;
import in.koreatech.koin.domain.owner.dto.sms.OwnerAccountCheckExistsRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerLoginRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerLoginResponse;
import in.koreatech.koin.domain.owner.dto.sms.OwnerPasswordResetVerifySmsRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerPasswordUpdateSmsRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerRegisterByPhoneRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerSendSmsRequest;
import in.koreatech.koin.domain.owner.dto.sms.OwnerSmsVerifyRequest;
import in.koreatech.koin.domain.owner.dto.sms.VerifySmsRequest;
import in.koreatech.koin.domain.owner.service.OwnerSmsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class SmsBasedOwnerController implements SmsBasedOwnerApi {

    private final OwnerSmsService ownerSmsService;

    @PostMapping("/owner/login")
    public ResponseEntity<OwnerLoginResponse> ownerLogin(
        @RequestBody @Valid OwnerLoginRequest request,
        @UserAgent UserAgentInfo userAgentInfo
    ) {
        OwnerLoginResponse response = ownerSmsService.ownerLogin(request, userAgentInfo);
        return ResponseEntity.created(URI.create("/"))
                .body(response);
    }

    @PostMapping("/owners/register/phone")
    public ResponseEntity<Void> registerByPhone(
        @Valid @RequestBody OwnerRegisterByPhoneRequest request
    ) {
        ownerSmsService.registerByPhone(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/owners/verification/sms")
    public ResponseEntity<Void> requestVerificationToRegisterBySms(
        @RequestBody @Valid VerifySmsRequest verifySmsRequest
    ) {
        ownerSmsService.requestSignUpSmsVerification(verifySmsRequest);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/owners/verification/code/sms")
    public ResponseEntity<OwnerVerifyResponse> codeVerification(
        @Valid @RequestBody OwnerSmsVerifyRequest request
    ) {
        OwnerVerifyResponse response = ownerSmsService.verifyCodeBySms(request);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/owners/password/reset/verification/sms")
    public ResponseEntity<Void> sendResetPasswordBySms(
        @Valid @RequestBody OwnerSendSmsRequest request
    ) {
        ownerSmsService.sendResetPasswordBySms(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/owners/password/reset/send/sms")
    public ResponseEntity<Void> sendVerifyCodeBySms(
        @Valid @RequestBody OwnerPasswordResetVerifySmsRequest request
    ) {
        ownerSmsService.verifyResetPasswordCodeBySms(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/owners/password/reset/sms")
    public ResponseEntity<Void> updatePasswordBySms(
        @Valid @RequestBody OwnerPasswordUpdateSmsRequest request
    ) {
        ownerSmsService.updatePasswordBySms(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/owners/exists/account")
    public ResponseEntity<Void> checkDuplicationOfPhoneNumber(
        @ModelAttribute("account")
        @Valid OwnerAccountCheckExistsRequest request
    ) {
        ownerSmsService.checkExistsAccount(request);
        return ResponseEntity.ok().build();
    }
}
