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
import in.koreatech.koin.domain.user.dto.AuthResponse;
import in.koreatech.koin.domain.user.dto.FindIdByEmailRequest;
import in.koreatech.koin.domain.user.dto.FindIdBySmsRequest;
import in.koreatech.koin.domain.user.dto.FindIdResponse;
import in.koreatech.koin.domain.user.dto.GeneralUserRegisterRequest;
import in.koreatech.koin.domain.user.dto.ResetPasswordByEmailRequest;
import in.koreatech.koin.domain.user.dto.ResetPasswordBySmsRequest;
import in.koreatech.koin.domain.user.dto.UserAccessTokenRequest;
import in.koreatech.koin.domain.user.dto.UserLoginRequest;
import in.koreatech.koin.domain.user.dto.UserLoginRequestV2;
import in.koreatech.koin.domain.user.dto.UserLoginResponse;
import in.koreatech.koin.domain.user.dto.UserResponse;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshRequest;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshResponse;
import in.koreatech.koin.domain.user.dto.UpdateUserRequest;
import in.koreatech.koin.domain.user.dto.UpdateUserResponse;
import in.koreatech.koin.domain.user.dto.validation.CheckEmailDuplicationRequest;
import in.koreatech.koin.domain.user.dto.validation.CheckLoginIdDuplicationRequest;
import in.koreatech.koin.domain.user.dto.validation.CheckNicknameDuplicationRequest;
import in.koreatech.koin.domain.user.dto.validation.CheckPhoneDuplicationRequest;
import in.koreatech.koin.domain.user.dto.validation.CheckUserPasswordRequest;
import in.koreatech.koin.domain.user.dto.validation.ExistsByEmailRequest;
import in.koreatech.koin.domain.user.dto.validation.ExistsByPhoneRequest;
import in.koreatech.koin.domain.user.dto.validation.ExistsByUserIdRequest;
import in.koreatech.koin.domain.user.dto.validation.MatchUserIdWithEmailRequest;
import in.koreatech.koin.domain.user.dto.validation.MatchUserIdWithPhoneNumberRequest;
import in.koreatech.koin.domain.user.service.UserService;
import in.koreatech.koin.domain.user.service.UserValidationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;
    private final UserValidationService userValidationService;

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
    public ResponseEntity<Void> generalUserRegisterV2(
        @RequestBody @Valid GeneralUserRegisterRequest request
    ) {
        userService.generalUserRegister(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/v2/users/login")
    public ResponseEntity<UserLoginResponse> loginV2(
        @RequestBody @Valid UserLoginRequestV2 request
    ) {
        UserLoginResponse response = userService.loginV2(request);
        return ResponseEntity.created(URI.create("/")).body(response);
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
        @ParameterObject @ModelAttribute(value = "address")
        @Valid CheckEmailDuplicationRequest request
    ) {
        userValidationService.checkDuplicatedEmail(request.email());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/check/phone")
    public ResponseEntity<Void> checkPhoneNumberExist(
        @ParameterObject @ModelAttribute(value = "phone")
        @Valid CheckPhoneDuplicationRequest request
    ) {
        userValidationService.checkDuplicatedPhoneNumber(request.phone());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/check/nickname")
    public ResponseEntity<Void> checkDuplicationOfNickname(
        @ParameterObject @ModelAttribute("nickname")
        @Valid CheckNicknameDuplicationRequest request
    ) {
        userValidationService.checkDuplicatedNickname(request.nickname());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/check/id")
    public ResponseEntity<Void> checkDuplicatedLoginId(
        @ParameterObject @ModelAttribute("id")
        @Valid CheckLoginIdDuplicationRequest request
    ) {
        userValidationService.checkDuplicatedLoginId(request.loginId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/check/password")
    public ResponseEntity<Void> checkPassword(
        @Valid @RequestBody CheckUserPasswordRequest request,
        @Auth(permit = {GENERAL, STUDENT, OWNER, COOP, COUNCIL}) Integer userId
    ) {
        userValidationService.checkPassword(request.password(), userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/id/exists")
    public ResponseEntity<Void> existsByUserId(
        @Valid @RequestBody ExistsByUserIdRequest request
    ) {
        userValidationService.existsByUserId(request.loginId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/phone/exists")
    public ResponseEntity<Void> existsByPhoneNumber(
        @Valid @RequestBody ExistsByPhoneRequest request
    ) {
        userValidationService.existsByPhoneNumber(request.phoneNumber());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/email/exists")
    public ResponseEntity<Void> existsByEmail(
        @Valid @RequestBody ExistsByEmailRequest request
    ) {
        userValidationService.existsByEmail(request.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/id/match/phone")
    public ResponseEntity<Void> matchUserIdWithPhoneNumber(
        @Valid @RequestBody MatchUserIdWithPhoneNumberRequest request
    ) {
        userValidationService.matchUserIdWithPhoneNumber(request.loginId(), request.phoneNumber());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/id/match/email")
    public ResponseEntity<Void> matchUserIdWithEmail(
        @Valid @RequestBody MatchUserIdWithEmailRequest request
    ) {
        userValidationService.matchUserIdWithEmail(request.loginId(), request.email());
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
