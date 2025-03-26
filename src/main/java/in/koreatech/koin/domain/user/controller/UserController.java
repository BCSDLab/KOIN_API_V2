package in.koreatech.koin.domain.user.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import java.net.URI;

import org.springdoc.core.annotations.ParameterObject;
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
import in.koreatech.koin.domain.user.dto.NicknameCheckExistsRequest;
import in.koreatech.koin.domain.user.dto.SendSmsVerificationRequest;
import in.koreatech.koin.domain.user.dto.UserAccessTokenRequest;
import in.koreatech.koin.domain.user.dto.UserLoginRequest;
import in.koreatech.koin.domain.user.dto.UserLoginResponse;
import in.koreatech.koin.domain.user.dto.UserPasswordCheckRequest;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshRequest;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshResponse;
import in.koreatech.koin.domain.user.dto.VerifySmsCodeRequest;
import in.koreatech.koin.domain.user.dto.VerifySmsCodeResponse;
import in.koreatech.koin.domain.user.service.UserService;
import in.koreatech.koin.domain.user.service.UserSmsService;
import in.koreatech.koin.domain.user.service.UserValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;
    private final UserValidationService userValidationService;
    private final UserSmsService userSmsService;

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

    @PostMapping("/user/sms/send")
    public ResponseEntity<Void> sendSignUpVerificationCode(
        @Valid @RequestBody SendSmsVerificationRequest request
    ) {
        userSmsService.sendSignUpVerificationCode(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/sms/verify")
    public ResponseEntity<VerifySmsCodeResponse> verifySignUpCode(
        @Valid @RequestBody VerifySmsCodeRequest request
    ) {
        log.info(request.toString());
        VerifySmsCodeResponse response = userSmsService.verifySignUpSmsCode(request);
        log.info(response.toString());
        return ResponseEntity.ok().body(response);
    }
}
