package in.koreatech.koin.domain.user.controller;

import static in.koreatech.koin.domain.user.model.UserType.*;

import java.net.URI;

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

import in.koreatech.koin.domain.user.dto.AuthResponse;
import in.koreatech.koin.domain.user.dto.AuthTokenRequest;
import in.koreatech.koin.domain.user.dto.CoopResponse;
import in.koreatech.koin.domain.user.dto.EmailCheckExistsRequest;
import in.koreatech.koin.domain.user.dto.FindPasswordRequest;
import in.koreatech.koin.domain.user.dto.NicknameCheckExistsRequest;
import in.koreatech.koin.domain.user.dto.StudentLoginRequest;
import in.koreatech.koin.domain.user.dto.StudentLoginResponse;
import in.koreatech.koin.domain.user.dto.StudentRegisterRequest;
import in.koreatech.koin.domain.user.dto.StudentResponse;
import in.koreatech.koin.domain.user.dto.StudentUpdateRequest;
import in.koreatech.koin.domain.user.dto.StudentUpdateResponse;
import in.koreatech.koin.domain.user.dto.UserLoginRequest;
import in.koreatech.koin.domain.user.dto.UserLoginResponse;
import in.koreatech.koin.domain.user.dto.UserPasswordChangeRequest;
import in.koreatech.koin.domain.user.dto.UserPasswordCheckRequest;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshRequest;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshResponse;
import in.koreatech.koin.domain.user.service.StudentService;
import in.koreatech.koin.domain.user.service.UserService;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.global.host.ServerURL;
import in.koreatech.koin.global.ipaddress.IpAddress;
import in.koreatech.koin.global.useragent.UserAgent;
import in.koreatech.koin.global.useragent.UserAgentInfo;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;
    private final StudentService studentService;

    @GetMapping("/user/student/me")
    public ResponseEntity<StudentResponse> getStudent(
        @Auth(permit = STUDENT) Integer userId
    ) {
        StudentResponse studentResponse = studentService.getStudent(userId);
        return ResponseEntity.ok().body(studentResponse);
    }

    @GetMapping("/user/coop/me")
    public ResponseEntity<CoopResponse> getCoop(
        @Auth(permit = COOP) Integer userId
    ) {
        CoopResponse coopResponse = userService.getCoop(userId);
        return ResponseEntity.ok().body(coopResponse);
    }

    @PutMapping("/user/student/me")
    public ResponseEntity<StudentUpdateResponse> updateStudent(
        @Auth(permit = STUDENT) Integer userId,
        @Valid @RequestBody StudentUpdateRequest request
    ) {
        StudentUpdateResponse studentUpdateResponse = studentService.updateStudent(userId, request);
        return ResponseEntity.ok(studentUpdateResponse);
    }

    @PostMapping("/user/login")
    public ResponseEntity<UserLoginResponse> login(
        @IpAddress String ipAddress,
        @UserAgent UserAgentInfo userAgentInfo,
        @RequestBody @Valid UserLoginRequest request
    ) {
        UserLoginResponse response = userService.login(ipAddress, userAgentInfo, request);
        return ResponseEntity.created(URI.create("/"))
            .body(response);
    }

    @PostMapping("/student/login")
    public ResponseEntity<StudentLoginResponse> studentLogin(
        @IpAddress String ipAddress,
        @UserAgent UserAgentInfo userAgentInfo,
        @RequestBody @Valid StudentLoginRequest request
    ) {
        StudentLoginResponse response = studentService.studentLogin(ipAddress, userAgentInfo, request);
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

    @PostMapping("/user/student/register")
    public ResponseEntity<Void> studentRegister(
        @Valid @RequestBody StudentRegisterRequest request,
        @ServerURL String serverURL
    ) {
        studentService.studentRegister(request, serverURL);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/user/authenticate")
    public ModelAndView authenticate(
        @ModelAttribute("auth_token")
        @Valid AuthTokenRequest request
    ) {
        return studentService.authenticate(request);
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

    @PostMapping("/user/find/password")
    public ResponseEntity<Void> findPassword(
        @RequestBody @Valid FindPasswordRequest request,
        @ServerURL String serverURL
    ) {
        studentService.findPassword(request, serverURL);
        return new ResponseEntity<>(HttpStatusCode.valueOf(201));
    }

    @PostMapping("/user/check/password")
    public ResponseEntity<Void> checkPassword(
        @Valid @RequestBody UserPasswordCheckRequest request,
        @Auth(permit = {STUDENT, OWNER, COOP}) Integer userId
    ) {
        userService.checkPassword(request, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/change/password/config")
    public ModelAndView checkResetToken(
        @ServerURL String serverUrl,
        @RequestParam("reset_token") String resetToken
    ) {
        return studentService.checkResetToken(resetToken, serverUrl);
    }

    @Hidden
    @PostMapping("/user/change/password/submit")
    public ResponseEntity<Void> changePassword(
        @RequestBody UserPasswordChangeRequest request,
        @RequestParam("reset_token") String resetToken
    ) {
        studentService.changePassword(request, resetToken);
        return ResponseEntity.ok().build();
    }
}
