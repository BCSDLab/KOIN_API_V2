package in.koreatech.koin.domain.owner.controller;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.owner.dto.CompanyNumberCheckRequest;
import in.koreatech.koin.domain.owner.dto.OwnerAccountCheckExistsRequest;
import in.koreatech.koin.domain.owner.dto.OwnerEmailVerifyRequest;
import in.koreatech.koin.domain.owner.dto.OwnerLoginRequest;
import in.koreatech.koin.domain.owner.dto.OwnerLoginResponse;
import in.koreatech.koin.domain.owner.dto.OwnerPasswordResetVerifyEmailRequest;
import in.koreatech.koin.domain.owner.dto.OwnerPasswordResetVerifySmsRequest;
import in.koreatech.koin.domain.owner.dto.OwnerPasswordUpdateEmailRequest;
import in.koreatech.koin.domain.owner.dto.OwnerPasswordUpdateSmsRequest;
import in.koreatech.koin.domain.owner.dto.OwnerRegisterByPhoneRequest;
import in.koreatech.koin.domain.owner.dto.OwnerRegisterRequest;
import in.koreatech.koin.domain.owner.dto.OwnerResponse;
import in.koreatech.koin.domain.owner.dto.OwnerSendEmailRequest;
import in.koreatech.koin.domain.owner.dto.OwnerSendSmsRequest;
import in.koreatech.koin.domain.owner.dto.OwnerSmsVerifyRequest;
import in.koreatech.koin.domain.owner.dto.OwnerVerifyResponse;
import in.koreatech.koin.domain.owner.dto.VerifyEmailRequest;
import in.koreatech.koin.domain.owner.dto.VerifySmsRequest;
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

    @PostMapping("/owners/verification/sms")
    public ResponseEntity<Void> requestVerificationToRegisterBySms(
        @RequestBody @Valid VerifySmsRequest verifySmsRequest
    ) {
        ownerService.requestSignUpSmsVerification(verifySmsRequest);
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

    @PostMapping("/owner/login")
    public ResponseEntity<OwnerLoginResponse> ownerLogin(
        @RequestBody @Valid OwnerLoginRequest request
    ) {
        OwnerLoginResponse response = ownerService.ownerLogin(request);
        return ResponseEntity.created(URI.create("/"))
            .body(response);
    }

    @PostMapping("/owners/register/phone")
    public ResponseEntity<Void> registerByPhone(
        @Valid @RequestBody OwnerRegisterByPhoneRequest request
    ) {
        ownerService.registerByPhone(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/owners/verification/code")
    public ResponseEntity<OwnerVerifyResponse> codeVerification(
        @Valid @RequestBody OwnerEmailVerifyRequest request
    ) {
        OwnerVerifyResponse response = ownerService.verifyCodeByEmail(request);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/owners/verification/code/sms")
    public ResponseEntity<OwnerVerifyResponse> codeVerification(
        @Valid @RequestBody OwnerSmsVerifyRequest request
    ) {
        OwnerVerifyResponse response = ownerService.verifyCodeBySms(request);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/owners/password/reset/verification")
    public ResponseEntity<Void> sendResetPasswordByEmail(
        @Valid @RequestBody OwnerSendEmailRequest request
    ) {
        ownerService.sendResetPasswordByEmail(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/owners/password/reset/verification/sms")
    public ResponseEntity<Void> sendResetPasswordBySms(
        @Valid @RequestBody OwnerSendSmsRequest request
    ) {
        ownerService.sendResetPasswordBySms(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/owners/password/reset/send")
    public ResponseEntity<Void> sendVerifyCode(
        @Valid @RequestBody OwnerPasswordResetVerifyEmailRequest request
    ) {
        ownerService.verifyResetPasswordCodeByEmail(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/owners/password/reset/send/sms")
    public ResponseEntity<Void> sendVerifyCodeBySms(
        @Valid @RequestBody OwnerPasswordResetVerifySmsRequest request
    ) {
        ownerService.verifyResetPasswordCodeBySms(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/owners/password/reset")
    public ResponseEntity<Void> updatePasswordByEmail(
        @Valid @RequestBody OwnerPasswordUpdateEmailRequest request
    ) {
        ownerService.updatePasswordByEmail(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/owners/password/reset/sms")
    public ResponseEntity<Void> updatePasswordBySms(
        @Valid @RequestBody OwnerPasswordUpdateSmsRequest request
    ) {
        ownerService.updatePasswordBySms(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/owners/check/company-number")
    public ResponseEntity<Void> checkCompanyNumber(
        @ModelAttribute("company_number")
        @Valid CompanyNumberCheckRequest request
    ) {
        ownerService.checkCompanyNumber(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/owners/check/account")
    public ResponseEntity<Void> checkDuplicationOfPhoneNumber(
        @ModelAttribute("account")
        @Valid OwnerAccountCheckExistsRequest request
    ) {
        ownerService.checkExistsAccount(request);
        return ResponseEntity.ok().build();
    }
}
