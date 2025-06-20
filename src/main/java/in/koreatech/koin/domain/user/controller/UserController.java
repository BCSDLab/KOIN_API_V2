package in.koreatech.koin.domain.user.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.admin.abtest.useragent.UserAgent;
import in.koreatech.koin.admin.abtest.useragent.UserAgentInfo;
import in.koreatech.koin.domain.user.dto.UserFindIdByEmailRequest;
import in.koreatech.koin.domain.user.dto.UserFindIdBySmsRequest;
import in.koreatech.koin.domain.user.dto.UserFindLoginIdResponse;
import in.koreatech.koin.domain.user.dto.UserLoginRequest;
import in.koreatech.koin.domain.user.dto.UserLoginRequestV2;
import in.koreatech.koin.domain.user.dto.UserLoginResponse;
import in.koreatech.koin.domain.user.dto.UserRefreshTokenRequest;
import in.koreatech.koin.domain.user.dto.UserRefreshTokenResponse;
import in.koreatech.koin.domain.user.dto.UserRegisterRequest;
import in.koreatech.koin.domain.user.dto.UserResetPasswordByEmailRequest;
import in.koreatech.koin.domain.user.dto.UserResetPasswordBySmsRequest;
import in.koreatech.koin.domain.user.dto.UserResponse;
import in.koreatech.koin.domain.user.dto.UserTypeResponse;
import in.koreatech.koin.domain.user.dto.UserUpdateRequest;
import in.koreatech.koin.domain.user.dto.UserUpdateResponse;
import in.koreatech.koin.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @GetMapping("/v2/users/me")
    public ResponseEntity<UserResponse> getUser(
        @Auth(permit = {GENERAL}) Integer userId
    ) {
        UserResponse userResponse = userService.getUser(userId);
        return ResponseEntity.ok().body(userResponse);
    }

    @GetMapping("/user/auth")
    public ResponseEntity<UserTypeResponse> getUserType(
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId
    ) {
        UserTypeResponse userTypeResponse = userService.getUserType(userId);
        return ResponseEntity.ok().body(userTypeResponse);
    }

    @PostMapping("/v2/users/register")
    public ResponseEntity<Void> register(
        @RequestBody @Valid UserRegisterRequest request
    ) {
        userService.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/v2/users/login")
    public ResponseEntity<UserLoginResponse> loginV2(
        @RequestBody @Valid UserLoginRequestV2 request,
        @UserAgent UserAgentInfo userAgentInfo
    ) {
        UserLoginResponse response = userService.loginV2(request, userAgentInfo);
        return ResponseEntity.created(URI.create("/")).body(response);
    }

    @PostMapping("/user/login")
    public ResponseEntity<UserLoginResponse> login(
        @RequestBody @Valid UserLoginRequest request,
        @UserAgent UserAgentInfo userAgentInfo
    ) {
        UserLoginResponse response = userService.login(request, userAgentInfo);
        return ResponseEntity.created(URI.create("/")).body(response);
    }

    @PostMapping("/user/refresh")
    public ResponseEntity<UserRefreshTokenResponse> refreshToken(
        @RequestBody @Valid UserRefreshTokenRequest request,
        @UserAgent UserAgentInfo userAgentInfo
    ) {
        UserRefreshTokenResponse response = userService.refresh(request, userAgentInfo);
        return ResponseEntity.created(URI.create("/")).body(response);
    }

    @PostMapping("/user/logout")
    public ResponseEntity<Void> logout(
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId,
        @UserAgent UserAgentInfo userAgentInfo
    ) {
        userService.logout(userId, userAgentInfo);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/id/find/sms")
    public ResponseEntity<UserFindLoginIdResponse> findIdBySmsVerification(
        @Valid @RequestBody UserFindIdBySmsRequest request
    ) {
        UserFindLoginIdResponse response = userService.findIdBySms(request);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/users/id/find/email")
    public ResponseEntity<UserFindLoginIdResponse> findIdByEmailVerification(
        @Valid @RequestBody UserFindIdByEmailRequest request
    ) {
        UserFindLoginIdResponse response = userService.findIdByEmail(request);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/users/password/reset/sms")
    public ResponseEntity<Void> resetPasswordBySmsVerification(
        @Valid @RequestBody UserResetPasswordBySmsRequest request
    ) {
        userService.resetPasswordBySms(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/password/reset/email")
    public ResponseEntity<Void> resetPasswordByEmailVerification(
        @Valid @RequestBody UserResetPasswordByEmailRequest request
    ) {
        userService.resetPasswordByEmail(request);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/v2/users/me")
    public ResponseEntity<UserUpdateResponse> updateUser(
        @Auth(permit = {GENERAL}) Integer userId,
        @Valid @RequestBody UserUpdateRequest request
    ) {
        UserUpdateResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/user")
    public ResponseEntity<Void> withdraw(
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId
    ) {
        userService.withdraw(userId);
        return ResponseEntity.noContent().build();
    }
}
