package in.koreatech.koin.domain.user.controller;

import static in.koreatech.koin.domain.user.model.UserType.COOP;
import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.net.URI;

import in.koreatech.koin.domain.student.dto.StudentLoginRequest;
import in.koreatech.koin.domain.student.dto.StudentLoginResponse;
import in.koreatech.koin.domain.student.dto.StudentRegisterRequest;
import in.koreatech.koin.domain.student.dto.StudentUpdateRequest;
import in.koreatech.koin.domain.student.dto.StudentUpdateResponse;
import in.koreatech.koin.domain.user.dto.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import in.koreatech.koin.domain.user.service.UserService;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.host.ServerURL;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @GetMapping("/user/coop/me")
    public ResponseEntity<CoopResponse> getCoop(
        @Auth(permit = COOP) Integer userId
    ) {
        CoopResponse coopResponse = userService.getCoop(userId);
        return ResponseEntity.ok().body(coopResponse);
    }

    @PostMapping("/user/login")
    public ResponseEntity<UserLoginResponse> login(
        @RequestBody @Valid UserLoginRequest request
    ) {
        UserLoginResponse response = userService.login(request);
        return ResponseEntity.created(URI.create("/"))
            .body(response);
    }

    @PostMapping("/user/logout")
    public ResponseEntity<Void> logout(
        @Auth(permit = {STUDENT, OWNER, COOP}) Integer userId
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

    @DeleteMapping("/user")
    public ResponseEntity<Void> withdraw(
        @Auth(permit = {STUDENT, OWNER, COOP}) Integer userId
    ) {
        userService.withdraw(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/check/email")
    public ResponseEntity<Void> checkUserEmailExist(
        @ModelAttribute(value = "address")
        @Valid EmailCheckExistsRequest request
    ) {
        userService.checkExistsEmail(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/check/nickname")
    public ResponseEntity<Void> checkDuplicationOfNickname(
        @ModelAttribute("nickname")
        @Valid NicknameCheckExistsRequest request
    ) {
        userService.checkUserNickname(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/auth")
    public ResponseEntity<AuthResponse> getAuth(
        @Auth(permit = {STUDENT, OWNER, COOP}) Integer userId
    ) {
        AuthResponse authResponse = userService.getAuth(userId);
        return ResponseEntity.ok().body(authResponse);
    }

    @GetMapping("/user/check/login")
    public ResponseEntity<Void> checkLogin(
            @ParameterObject @ModelAttribute(value = "access_token")
            @Valid UserAccessTokenRequest request
    ) {
        userService.checkLogin(request.accessToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/check/password")
    public ResponseEntity<Void> checkPassword(
        @Valid @RequestBody UserPasswordCheckRequest request,
        @Auth(permit = {STUDENT, OWNER, COOP}) Integer userId
    ) {
        userService.checkPassword(request, userId);
        return ResponseEntity.ok().build();
    }
}
