package in.koreatech.koin.domain.user.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import java.net.URI;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.domain.user.dto.AuthResponse;
import in.koreatech.koin.domain.user.dto.EmailCheckExistsRequest;
import in.koreatech.koin.domain.user.dto.GeneralUserRegisterRequest;
import in.koreatech.koin.domain.user.dto.NicknameCheckExistsRequest;
import in.koreatech.koin.domain.user.dto.PhoneCheckExistsRequest;
import in.koreatech.koin.domain.user.dto.UserAccessTokenRequest;
import in.koreatech.koin.domain.user.dto.UserLoginRequest;
import in.koreatech.koin.domain.user.dto.UserLoginResponse;
import in.koreatech.koin.domain.user.dto.UserPasswordCheckRequest;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshRequest;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshResponse;
import in.koreatech.koin.domain.user.dto.verification.CheckEmailRequest;
import in.koreatech.koin.domain.user.dto.verification.CheckPhoneNumberRequest;
import in.koreatech.koin.domain.user.dto.verification.CheckUserIdRequest;
import in.koreatech.koin.domain.user.dto.verification.CheckUserIdWithEmailRequest;
import in.koreatech.koin.domain.user.dto.verification.CheckUserIdWithPhoneNumberRequest;
import in.koreatech.koin.domain.user.dto.verification.EmailFindIdRequest;
import in.koreatech.koin.domain.user.dto.verification.EmailResetPasswordRequest;
import in.koreatech.koin.domain.user.dto.verification.FindIdResponse;
import in.koreatech.koin.domain.user.dto.verification.SmsFindIdRequest;
import in.koreatech.koin.domain.user.dto.verification.SmsResetPasswordRequest;
import in.koreatech.koin.domain.user.service.UserService;
import in.koreatech.koin.domain.user.service.UserValidationService;
import in.koreatech.koin.domain.user.service.UserVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;
    private final UserValidationService userValidationService;
    private final UserVerificationService userVerificationService;

    @PostMapping("/v2/user/general/register")
    public ResponseEntity<Void> generalUserRegisterV2(
        @RequestBody @Valid GeneralUserRegisterRequest request
    ) {
        userService.generalUserRegister(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/user/login")
    public ResponseEntity<UserLoginResponse> login(
        @RequestBody @Valid UserLoginRequest request
    ) {
        UserLoginResponse response = userService.login(request);
        return ResponseEntity.created(URI.create("/")).body(response);
    }

    @PostMapping("/user/logout")
    public ResponseEntity<Void> logout(
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId
    ) {
        userService.logout(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/refresh")
    public ResponseEntity<UserTokenRefreshResponse> refresh(
        @RequestBody @Valid UserTokenRefreshRequest request
    ) {
        UserTokenRefreshResponse tokenGroupResponse = userService.refresh(request);
        return ResponseEntity.created(URI.create("/"))
            .body(tokenGroupResponse);
    }

    @GetMapping("/user/auth")
    public ResponseEntity<AuthResponse> getAuth(
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId
    ) {
        AuthResponse authResponse = userService.getAuth(userId);
        return ResponseEntity.ok().body(authResponse);
    }

    @DeleteMapping("/user")
    public ResponseEntity<Void> withdraw(
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId
    ) {
        userService.withdraw(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/check/login")
    public ResponseEntity<Void> checkLogin(
        @ParameterObject @ModelAttribute(value = "access_token")
        @Valid UserAccessTokenRequest request
    ) {
        userService.checkLogin(request.accessToken());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/check/email")
    public ResponseEntity<Void> checkUserEmailExist(
        @ModelAttribute(value = "address")
        @Valid EmailCheckExistsRequest request
    ) {
        userValidationService.checkExistsEmail(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/check/phone")
    public ResponseEntity<Void> checkPhoneNumberExist(
        @ModelAttribute(value = "phone")
        @Valid PhoneCheckExistsRequest request
    ) {
        userValidationService.checkExistsPhoneNumber(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/check/nickname")
    public ResponseEntity<Void> checkDuplicationOfNickname(
        @ModelAttribute("nickname")
        @Valid NicknameCheckExistsRequest request
    ) {
        userValidationService.checkUserNickname(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/check/password")
    public ResponseEntity<Void> checkPassword(
        @Valid @RequestBody UserPasswordCheckRequest request,
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId
    ) {
        userValidationService.checkPassword(request, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/id/exists")
    public ResponseEntity<Void> existsByUserId(
        @Valid @RequestBody CheckUserIdRequest request
    ) {
        userValidationService.existsByUserId(request.userId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/phone/exists")
    public ResponseEntity<Void> existsByPhoneNumber(
        @Valid @RequestBody CheckPhoneNumberRequest request
    ) {
        userValidationService.existsByPhoneNumber(request.phoneNumber());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/email/exists")
    public ResponseEntity<Void> existsByEmail(
        @Valid @RequestBody CheckEmailRequest request
    ) {
        userValidationService.existsByEmail(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/id/match/phone")
    public ResponseEntity<Void> matchUserIdWithPhoneNumber(
        @Valid @RequestBody CheckUserIdWithPhoneNumberRequest request
    ) {
        userValidationService.matchUserIdWithPhoneNumber(request.userId(), request.phoneNumber());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/id/match/email")
    public ResponseEntity<Void> matchUserIdWithEmail(
        @Valid @RequestBody CheckUserIdWithEmailRequest request
    ) {
        userValidationService.matchUserIdWithEmail(request.userId(), request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/id/find/sms")
    public ResponseEntity<FindIdResponse> findIdBySmsVerification(
        @Valid @RequestBody SmsFindIdRequest request
    ) {
        String userId = userVerificationService.findIdByVerification(request.phoneNumber());
        return ResponseEntity.ok().body(FindIdResponse.from(userId));
    }

    @PostMapping("/user/id/find/email")
    public ResponseEntity<FindIdResponse> findIdByEmailVerification(
        @Valid @RequestBody EmailFindIdRequest request
    ) {
        String userId = userVerificationService.findIdByVerification(request.email());
        return ResponseEntity.ok().body(FindIdResponse.from(userId));
    }

    @PostMapping("/user/password/reset/sms")
    public ResponseEntity<Void> resetPasswordBySmsVerification(
        @Valid @RequestBody SmsResetPasswordRequest request
    ) {
        userVerificationService.resetPasswordByVerification(request.userId(), request.phoneNumber(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/password/reset/email")
    public ResponseEntity<Void> resetPasswordByEmailVerification(
        @Valid @RequestBody EmailResetPasswordRequest request
    ) {
        userVerificationService.resetPasswordByVerification(null, request.email(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}
