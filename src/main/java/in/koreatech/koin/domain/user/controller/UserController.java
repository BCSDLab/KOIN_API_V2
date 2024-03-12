package in.koreatech.koin.domain.user.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import in.koreatech.koin.domain.user.dto.StudentResponse;
import in.koreatech.koin.domain.user.dto.UserLoginRequest;
import in.koreatech.koin.domain.user.dto.UserLoginResponse;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshRequest;
import in.koreatech.koin.domain.user.dto.UserTokenRefreshResponse;
import static in.koreatech.koin.domain.user.model.UserType.STUDENT;
import in.koreatech.koin.domain.user.service.StudentService;
import in.koreatech.koin.domain.user.service.UserService;
import in.koreatech.koin.global.auth.Auth;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
    public ResponseEntity<UserTokenRefreshResponse> withdraw(
        @Auth(permit = {STUDENT}) Long userId
    ) {
        userService.withdraw(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/check/email")
    public ResponseEntity<Void> checkUserEmailExist(
        @RequestParam(value = "address")
        @Email(message = "이메일 형식을 지켜주세요.")
        @NotBlank(message = "이메일을 입력해주세요.")
        String email
    ) {
        userService.checkExistsEmail(email);
        return ResponseEntity.ok().build();
    }
}
