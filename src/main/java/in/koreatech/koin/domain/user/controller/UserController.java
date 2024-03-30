package in.koreatech.koin.domain.user.controller;

import static in.koreatech.koin.domain.user.model.UserType.STUDENT;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.user.dto.EmailCheckExistsRequest;
import in.koreatech.koin.domain.user.dto.NicknameCheckExistsRequest;
import in.koreatech.koin.domain.user.dto.StudentRegisterRequest;
import in.koreatech.koin.domain.user.dto.StudentResponse;
import in.koreatech.koin.domain.user.dto.StudentUpdateRequest;
import in.koreatech.koin.domain.user.dto.StudentUpdateResponse;
import in.koreatech.koin.domain.user.dto.UserLoginRequest;
import in.koreatech.koin.domain.user.dto.UserLoginResponse;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshRequest;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshResponse;

import static in.koreatech.koin.domain.user.model.UserType.OWNER;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import in.koreatech.koin.domain.user.service.StudentService;
import in.koreatech.koin.domain.user.service.UserService;
import in.koreatech.koin.global.auth.Auth;
import in.koreatech.koin.domain.user.dto.NotificationPermitRequest;
import in.koreatech.koin.domain.user.dto.NotificationStatusResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;
    private final StudentService studentService;

    @GetMapping("/user/student/me")
    public ResponseEntity<StudentResponse> getStudent(
        @Auth(permit = STUDENT) Long userId
    ) {
        StudentResponse studentResponse = studentService.getStudent(userId);
        return ResponseEntity.ok().body(studentResponse);
    }

    @PutMapping("/user/student/me")
    public ResponseEntity<StudentUpdateResponse> updateStudent(
        @Auth(permit = STUDENT) Long userId,
        @Valid @RequestBody StudentUpdateRequest request
    ) {
        StudentUpdateResponse studentUpdateResponse = studentService.updateStudent(userId, request);
        return ResponseEntity.ok(studentUpdateResponse);
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
        @Auth(permit = {STUDENT}) Long userId) {
        userService.logout(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/refresh")
    public ResponseEntity<UserTokenRefreshResponse> refresh(
        @RequestBody @Valid UserTokenRefreshRequest request
    ) {
        UserTokenRefreshResponse tokenGroupResponse = userService.refresh(request);
        return ResponseEntity.ok().body(tokenGroupResponse);
    }

    @DeleteMapping("/user")
    public ResponseEntity<Void> withdraw(
        @Auth(permit = {STUDENT}) Long userId
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

    @Override
    public ResponseEntity<Void> studentRegister(StudentRegisterRequest studentRegisterRequest) {
        return null;
    }

    @GetMapping("/user/check/nickname")
    public ResponseEntity<Void> checkDuplicationOfNickname(
        @ModelAttribute("nickname")
        @Valid NicknameCheckExistsRequest request) {
        userService.checkUserNickname(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/notification")
    public ResponseEntity<NotificationStatusResponse> checkNotificationStatus(
        @Auth(permit = {STUDENT, OWNER}) Long memberId
    ) {
        return ResponseEntity.ok(userService.checkNotification(memberId));
    }

    @PostMapping("/user/notification")
    public ResponseEntity<Void> permitNotification(
        @Auth(permit = {STUDENT, OWNER}) Long memberId,
        @Valid @RequestBody NotificationPermitRequest request
    ) {
        userService.permitNotification(memberId, request.deviceToken());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/user/notification")
    public ResponseEntity<Void> rejectNotification(
        @Auth(permit = {STUDENT, OWNER}) Long memberId
    ) {
        userService.rejectNotification(memberId);
        return ResponseEntity.ok().build();
    }
}
