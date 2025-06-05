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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin._common.auth.Auth;
import in.koreatech.koin.admin.abtest.useragent.UserAgent;
import in.koreatech.koin.admin.abtest.useragent.UserAgentInfo;
import in.koreatech.koin.domain.user.dto.AuthResponse;
import in.koreatech.koin.domain.user.dto.FindIdByEmailRequest;
import in.koreatech.koin.domain.user.dto.FindIdBySmsRequest;
import in.koreatech.koin.domain.user.dto.FindIdResponse;
import in.koreatech.koin.domain.user.dto.RefreshUserTokenRequest;
import in.koreatech.koin.domain.user.dto.RefreshUserTokenResponse;
import in.koreatech.koin.domain.user.dto.RegisterUserRequest;
import in.koreatech.koin.domain.user.dto.ResetPasswordByEmailRequest;
import in.koreatech.koin.domain.user.dto.ResetPasswordBySmsRequest;
import in.koreatech.koin.domain.user.dto.UpdateUserRequest;
import in.koreatech.koin.domain.user.dto.UpdateUserResponse;
import in.koreatech.koin.domain.user.dto.UserAccessTokenRequest;
import in.koreatech.koin.domain.user.dto.UserLoginRequest;
import in.koreatech.koin.domain.user.dto.UserLoginRequestV2;
import in.koreatech.koin.domain.user.dto.UserLoginResponse;
import in.koreatech.koin.domain.user.dto.UserResponse;
import in.koreatech.koin.domain.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @GetMapping("/v2/users/me")
    public ResponseEntity<UserResponse> getUserV2(
        @Auth(permit = {GENERAL}) Integer userId
    ) {
        UserResponse userResponse = userService.getUserV2(userId);
        return ResponseEntity.ok().body(userResponse);
    }

    @PutMapping("/v2/users/me")
    public ResponseEntity<UpdateUserResponse> updateUserV2(
        @Auth(permit = {GENERAL}) Integer userId,
        @Valid @RequestBody UpdateUserRequest request
    ) {
        UpdateUserResponse response = userService.updateUserV2(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/v2/users/register")
    public ResponseEntity<Void> registerUserV2(
        @RequestBody @Valid RegisterUserRequest request
    ) {
        userService.userRegister(request);
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

    @PostMapping("/user/logout")
    public ResponseEntity<Void> logout(
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId,
        @UserAgent UserAgentInfo userAgentInfo
    ) {
        userService.logout(userId, userAgentInfo);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/refresh")
    public ResponseEntity<RefreshUserTokenResponse> refresh(
        @RequestBody @Valid RefreshUserTokenRequest request,
        @UserAgent UserAgentInfo userAgentInfo
    ) {
        RefreshUserTokenResponse tokenGroupResponse = userService.refresh(request, userAgentInfo);
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

    @PostMapping("/users/id/find/sms")
    public ResponseEntity<FindIdResponse> findIdBySmsVerification(
        @Valid @RequestBody FindIdBySmsRequest request
    ) {
        String userId = userService.findIdBySms(request.phoneNumber());
        return ResponseEntity.ok().body(FindIdResponse.from(userId));
    }

    @PostMapping("/users/id/find/email")
    public ResponseEntity<FindIdResponse> findIdByEmailVerification(
        @Valid @RequestBody FindIdByEmailRequest request
    ) {
        String userId = userService.findIdByEmail(request.email());
        return ResponseEntity.ok().body(FindIdResponse.from(userId));
    }

    @PostMapping("/users/password/reset/sms")
    public ResponseEntity<Void> resetPasswordBySmsVerification(
        @Valid @RequestBody ResetPasswordBySmsRequest request
    ) {
        userService.resetPasswordBySms(request.loginId(), request.phoneNumber(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/password/reset/email")
    public ResponseEntity<Void> resetPasswordByEmailVerification(
        @Valid @RequestBody ResetPasswordByEmailRequest request
    ) {
        userService.resetPasswordByEmail(request.loginId(), request.email(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}
